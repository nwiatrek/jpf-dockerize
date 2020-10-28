# JPF-DOCKERIZED

This repo is for owning and creating the `jpf-dockerized` image on dockerhub. The repo sets up the java 1.8 version as well as gradle 5-4-1.
JPF is a wonderful tool but can be a huge pain to setup. `jpf-dockerized` takes care of the setup by making sure the correct java and gradle version are installed so you can write jpf code without having to spend time setting things up.

### How to Use

use the command: `docker pull nwiatrek/jpf-dockerized`

After that run the docker image using volumes so you can write code on your machine and it get moved over to the docker container automatically.

### Helpful links

https://docs.docker.com/compose/compose-file/#build
https://docs.docker.com/compose/gettingstarted/

An example docker-compose would look like the following:

```
version: '3.8'
services:
    jpf:
        image: nwiatrek/jpf-dockerized

        stdin_open: true
        tty: true
        volumes:
            - ./example-code:/code
```
