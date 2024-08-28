FROM armdocker.rnd.ericsson.se/dockerhub-ericsson-remote/python:3.9.4-slim-buster

RUN apt-get update && \
    apt-get install -y --no-install-recommends

RUN pip install poetry==1.5.0

# A locale needs to be installed and set for later use by some python packages like click
ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8

# Store all of the packages under the /usr/src/app/ directory
RUN mkdir -p /usr/src/app/
WORKDIR /usr/src/app
COPY poetry.lock pyproject.toml /usr/src/app/

# We turn off virtual env as not needed inside the container
RUN poetry config virtualenvs.create false

# Install dependencies
RUN poetry install --no-interaction --no-ansi

EXPOSE 22

CMD ["/usr/sbin/sshd","-D"]
