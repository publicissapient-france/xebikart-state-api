FROM golang:1.12.4-alpine AS builder
LABEL project=xebikart
LABEL maintainer=xebikart-team-dashboard

# Git is needed by `go get`
RUN apk add -u git \
      && rm -rf /var/cache/apk/*

RUN go get -u github.com/golang/dep/cmd/dep

WORKDIR /go/src/github.com/xebia-france/xebikart-state-api
ADD Gopkg.toml Gopkg.lock ./
RUN dep ensure --vendor-only

ADD . ./
RUN go build -o main .


# Second part of the multi-stage Dockerfile - build the resulting minimal image
FROM alpine:3.9.3
LABEL project=xebikart
LABEL maintainer=xebikart-team-dashboard
EXPOSE 80

RUN apk add -u ca-certificates \
      && rm -rf /var/cache/apk/*

COPY --from=builder /go/src/github.com/xebia-france/xebikart-state-api/main /xebikart-state-api
CMD /xebikart-state-api
