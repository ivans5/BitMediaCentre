package main

import (
	"fmt"
	//appsv1 "k8s.io/api/apps/v1"
	apiv1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	//"k8s.io/client-go/tools/clientcmd" //for BuildConfigFromFlags(kubeconfig, ...)...
	"k8s.io/client-go/rest" //for InClusterConfig()...
	//"k8s.io/client-go/util/homedir"
	//"k8s.io/client-go/util/retry"
        "net/http"
        "strings"
        "encoding/hex"
	"math/rand"
	"time"
	"io/ioutil"
	"encoding/json"
)

var labelsToConcatenate []string = []string{"publickeya","publickeyb","publickeyc","publickeyd","publickeye","publickeyf"}

type ClientPack struct  {
  clientset *kubernetes.Clientset
}

type JsonRequest struct {
  EncryptedKeySpec string
  EncryptedIvSpec string
  EncryptedPayload string
}

func searchForNode(clientset *kubernetes.Clientset, id string) (*apiv1.Node, int)  {
   nodeList, err := clientset.CoreV1().Nodes().List(metav1.ListOptions{})
  if err != nil {
    return nil, 500
  }
  for _, x := range nodeList.Items  {
    if _, ok := x.ObjectMeta.Labels["machine-id"]; ok && x.ObjectMeta.Labels["machine-id"] == id  {
      //check if node is ready:
      for _, y := range x.Status.Conditions  {
        if y.Status == "True" && y.Type == "Ready"  {
           return &x,200
        }
      }
      return nil,503 //HTTP 503 - Service temporarily unavailable
    }
  }
  return nil,404
}


//return the DER binary contents to the caller
func (clientPack *ClientPack) getPublicKey(rw http.ResponseWriter, req *http.Request) {
  theId := strings.Split(req.URL.Path,"/")[2]
  //TODO: check id for length and charset...

  theNode, http_status_code := searchForNode(clientPack.clientset, theId)
  if theNode == nil  {
    rw.WriteHeader(http_status_code)
    return
  }

  var hexDump string
  for _, x := range(labelsToConcatenate)  {
    if val, ok := theNode.ObjectMeta.Labels[x]; ok  {
      hexDump = hexDump + val
    } else {
      rw.WriteHeader(500)
      return
    }
  }

  src := []byte(hexDump)
  dst := make([]byte, hex.DecodedLen(len(src)))
  n, err := hex.Decode(dst, src)
  if err != nil {
    rw.WriteHeader(500)
    return
  }
  rw.Header().Add("Content-Type","application/octet-stream")
  rw.Header().Add("Content-Length",fmt.Sprintf("%d",n))
  rw.Write(dst) 

}

func (clientPack *ClientPack) startDownload(rw http.ResponseWriter, req *http.Request) {
  theId := strings.Split(req.URL.Path,"/")[2]
  //TODO: check id for length and charset...
  //TODO: verify POST was used?

  b, err := ioutil.ReadAll(req.Body)
  req.Body.Close() //now's good...
  if err != nil {
	http.Error(rw, err.Error(), 500)
	return
  }
  
  fmt.Printf("theId=%s theBody = %s\n",theId,b)

  var jsonRequest JsonRequest
  err = json.Unmarshal(b, &jsonRequest)
  if err != nil {
    fmt.Println("Couldnt unmarshall: "+err.Error())
    http.Error(rw, err.Error(), 500)
    return
  }

  theNode, http_status_code := searchForNode(clientPack.clientset, theId)
  if theNode == nil  {
    fmt.Printf("searchForNode: %d\n",http_status_code)
    http.Error(rw, err.Error(), http_status_code)
    return
  }

  //Create namespace if not already exists:
  newNamespace := apiv1.Namespace{}
  newNamespace.ObjectMeta.Name = strings.ToLower("namespace-"+string(theId))

  _, err = clientPack.clientset.CoreV1().Namespaces().Create(&newNamespace)
  if err != nil && strings.Count(err.Error(),"already exists") == 0 {
    fmt.Printf("create namespace: %s\n",err.Error())
    http.Error(rw, err.Error(), 500)
    return
  }

  //Create pod:
  newPod := apiv1.Pod{}
  newPod.ObjectMeta = metav1.ObjectMeta{Name: fmt.Sprintf("myaria2pod-%d",10000+rand.Intn(89999)), Namespace: newNamespace.ObjectMeta.Name} 
  hostPathTypeDirectory := (apiv1.HostPathType) ("Directory")  //so obnoxious !!
  var myIdNumber int64 = 1000
  newPod.Spec = apiv1.PodSpec{
          SecurityContext: &apiv1.PodSecurityContext{RunAsUser: &myIdNumber, RunAsGroup: &myIdNumber},
          DNSPolicy: "None",
          DNSConfig: &apiv1.PodDNSConfig{Nameservers: []string{"8.8.8.8","1.1.1.1"}},
          Tolerations: []apiv1.Toleration{{Key: "key", Value: "value", Effect: "NoSchedule"}},
	  NodeName: theNode.ObjectMeta.Name,
	  RestartPolicy: "Never",
          Volumes: []apiv1.Volume{ 
                                   apiv1.Volume{Name: "homepcuser", VolumeSource: apiv1.VolumeSource{HostPath: &apiv1.HostPathVolumeSource{Path: "/home/pcuser", Type: &hostPathTypeDirectory }}}},
	  Containers: []apiv1.Container{{Name: "myaria2", Image: "ivans3/myaria2:latest", 
                                         VolumeMounts: []apiv1.VolumeMount{{Name: "start", MountPath: "/start"},
                                                 {Name: "homepcuser", MountPath: "/homepcuser"}},
	                                 Env: []apiv1.EnvVar{ 
						 {Name: "ENCRYPTED_PAYLOAD", Value: jsonRequest.EncryptedPayload},
						 {Name: "ENCRYPTED_KEY_SPEC", Value: jsonRequest.EncryptedKeySpec},
						 {Name: "ENCRYPTED_IV_SPEC", Value: jsonRequest.EncryptedIvSpec}}}}}
					 

  _, err = clientPack.clientset.CoreV1().Pods(newNamespace.ObjectMeta.Name).Create(&newPod)
  if err != nil  {
    fmt.Printf("create pod: %s\n",err.Error())
    http.Error(rw, err.Error(), 500)
    return
  }

  //fmt.Printf("http_status_code = %d\n",http_status_code)
}

func (clientPack *ClientPack) ServeHTTP(rw http.ResponseWriter, req *http.Request) {
  if strings.HasPrefix(req.URL.Path,"/getpublickey/")  {
    clientPack.getPublicKey(rw, req)
  } else if strings.HasPrefix(req.URL.Path,"/startdownload/")  {
    clientPack.startDownload(rw, req)
  }
}

func main()  {
  config, err := rest.InClusterConfig()
  if err != nil {
    panic(err)
  }
  /*
  config, err := clientcmd.BuildConfigFromFlags("", "/var/lib/rancher/k3s/server/cred/admin.kubeconfig")
  if err != nil {
    panic(err)
  }
  */
  clientset, err := kubernetes.NewForConfig(config)
  if err != nil {
    panic(err)
  }


  clientPack := &ClientPack{clientset}
  rand.Seed(time.Now().UTC().UnixNano())

  http.ListenAndServe(":8080", clientPack)
}
