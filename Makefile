#
#	Build and install this Golang project
#

BINARY=xebikart-state-api
RELEASE_DIR=release/
PKGS := $(shell go list ./... | grep -v /vendor)

PLATFORMS=darwin linux windows
ARCHITECTURES=386 amd64

.PHONY: test
test: dep format
	go test -count=1 $(PKGS)

.PHONY: format
format: dep
	go fmt $(PKGS)

go.mod:
	go mod init

.PHONY: dep
dep: go.mod
	GO111MODULE=on go get

$(RELEASE_DIR): 
	mkdir -p $@ || true

.PHONY: build
build: dep  
	$(foreach GOOS, $(PLATFORMS),\
	$(foreach GOARCH, $(ARCHITECTURES), $(shell export GOOS=$(GOOS); export GOARCH=$(GOARCH); go build -v -o $(RELEASE_DIR)/$(BINARY)-$(GOOS)-$(GOARCH))))

.PHONY: clean
clean:
	rm -rf $(RELEASE_DIR) || true