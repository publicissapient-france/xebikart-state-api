FROM golang:1.12.4-alpine
LABEL project=xebikart
LABEL maintainer=xebikart-team-dashboard

# Git is needed by `go get`
RUN apk add -u git \
      && rm -rf /var/cache/apk/*

RUN go get -u github.com/golang/dep/cmd/dep
ADD . /go/src/github.com/xebia-france/xebikart-state-api
WORKDIR /go/src/github.com/xebia-france/xebikart-state-api
RUN dep ensure
RUN go build -o main . 


# Second part of the multi-stage Dockerfile - build the resulting minimal image
FROM alpine:3.9.3
LABEL project=xebikart
LABEL maintainer=xebikart-team-dashboard
EXPOSE 8080

RUN apk add -u ca-certificates \
      && rm -rf /var/cache/apk/*

COPY --from=0 /go/src/xebia-france/xebikart-state-api/main /xebikart-state-api
CMD /xebikart-state-api
