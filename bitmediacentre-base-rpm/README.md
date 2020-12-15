This rpm contains the /start and /env.sh symlinking service,

And is required because:
  - root folder is immutable and cant easily add items to it...
  - extra files in /etc can get wiped out under some conditions...
  - https://github.com/coreos/rpm-ostree/issues/337 (open after 4 years...)

Note:Fedora[-iot] has a default-deny preset! (see: https://fedoraproject.org/wiki/Features/PackagePresets )


```
rpmbuild -bb rpmbuild/SPECS/bitmediacentre-base.spec 
```
