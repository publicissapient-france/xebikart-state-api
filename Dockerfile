FROM golang:1.12.4-alpine AS builder
LABEL project=xebikart
LABEL maintainer=xebikart-team-dashboard

# Git is needed by `go get`
RUN apk add -u git \
      && rm -rf /var/cache/apk/*

WORKDIR /var/xebikart-state-api
ADD go.mod .
RUN go mod tidy

ADD . ./
RUN go build -o main .

# Second part of the multi-stage Dockerfile - build the resulting minimal image
FROM alpine:3.9.3
LABEL project=xebikart
LABEL maintainer=xebikart-team-dashboard
EXPOSE 80

RUN apk add -u ca-certificates \
      && rm -rf /var/cache/apk/*

COPY --from=builder /var/xebikart-state-api/main /xebikart-state-api
CMD /xebikart-state-api
