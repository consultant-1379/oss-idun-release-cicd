# IDUN CICD CLI / EIAP CICD CLI

[TOC]

## Introduction

The EIAP CICD CLI

## Prerequisites

idun_cicd_cli is meant to be ran inside a Docker container, however it can also be ran inside a python virtual environment.
* For running with docker, you need Docker installed on the system.
* For running without docker, you need to have Python Poetry installed.


## Running idun_cicd_cli with Docker

To run idun_cicd_cli with Docker, you can simply use "docker run" and point to the latest version of idun_cicd_cli in the Ericsson Docker Images Repository:
* armdocker.rnd.ericsson.se/proj-eric-oss-dev-test/oss-idun-cicd-cli

Example Usage:

```shell
docker run --rm --name idun_cicd_cli armdocker.rnd.ericsson.se/proj-eric-oss-dev-test/oss-idun-cicd-cli:latest --help
```

## Running idun_cicd_cli without Docker

> Make sure to run commands at the root level of the idun_cicd_cli repo.
> If you don't have the repo code yet,
> please go to [idun_cicd_cli](https://gerrit-gamma.gic.ericsson.se/#/admin/projects/OSS/com.ericsson.oss.cicd/oss-idun-release-cicd)
> If you are having issues running poetry commands try install distutils
> ```shell
> sudo apt install python3.9-distutils
> ```

To run idun_cicd_cli without Docker, make sure to have Python Poetry installed by running the following command:
```shell
pip install poetry
```


Then you must enable a Poetry virtual environment:

```shell
poetry shell
```

Install all the dependencies inside this virtual environment:

 ```shell
poetry install
```

Now run the tool. Example Usage:

```shell
python -m idun_cicd_cli --help
```
### Testing idun_cicd_cli without Docker
Make sure the Python version is set correctly
```shell
poetry env use $(which python3.9)
```
```shell
poetry run pytest
```

### Linting idun_cicd_cli without Docker
> Make Sure you have pylint & flake8 installed
> ```shell
> pip install pylint
> pip install flake8
```
#### Usage
```shell
poetry run  pylint --output-format=colorized --score=y "$(git diff --name-only --staged '*.py')"
poetry run flake8 "$(git diff --name-only --staged '*.py')"

```

## Contact

idun_cicd_cli is developed by Team Thunderbee.

For any queries, bugs or improvement suggestions please contact:
* Thunderbee <PDLENMCOUN@pdl.internal.ericsson.com>
