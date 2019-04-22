FROM golang:1.12.4-alpine
LABEL project=xebikart
LABEL maintainer=xebikart-team-dashboard

ADD . /go/src/xebia-france/xebikart-state-api 
WORKDIR /go/src/xebia-france/xebikart-state-api 
# Git is needed by `go get`
RUN apk add -u git \
      && rm -rf /var/cache/apk/*

RUN go get -u github.com/golang/dep/cmd/dep
RUN dep ensure
RUN go build -o main . 
EXPOSE 8080
CMD ["/go/src/xebia-france/xebikart-state-api/main"]
