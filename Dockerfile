FROM golang:latest 
RUN mkdir /app 
ADD . /go/src/xebia-france/xebikart-state-api 
WORKDIR /go/src/xebia-france/xebikart-state-api 
RUN go get -u github.com/golang/dep/cmd/dep
RUN dep ensure
RUN go build -o main . 
EXPOSE 8080
CMD ["/go/src/xebia-france/xebikart-state-api/main"]