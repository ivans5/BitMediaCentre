# tvision
NOTE: This is a Fork of a Fork of Sergio Sigala's Turbo Vision Port, fixed to build modern autotools.

## Build Steps on Fedora

1.
```
sudo dnf install autoconf automake gcc-c++ libtool ncurses-devel gpm-devel make
```

2.
```
autoreconf -fi
```

3.
```
./configure CXXFLAGS='-g -O2 -w -std=c++03' --disable-linux-gpm
```

4.
```
make
```
