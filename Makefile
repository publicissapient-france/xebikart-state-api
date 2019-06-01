#
#	Build and install this Golang project
#

BINARY=xebikart-state-api
RELEASE_DIR=release/
PKGS := $(shell go list ./... | grep -v /vendor)

PLATFORMS=darwin linux windows
ARCHITECTURES=386 amd64

.PHONY: test
test: format
	GO111MODULE=on go test -count=1 $(PKGS)

.PHONY: format
format: 
	go fmt $(PKGS)

go.mod:
	GO111MODULE=on go mod init

$(RELEASE_DIR): 
	mkdir -p $@ || true

.PHONY: build
build: 
	$(foreach GOOS, $(PLATFORMS),\
	$(foreach GOARCH, $(ARCHITECTURES), $(shell export GO111MODULE=on; export GOOS=$(GOOS); export GOARCH=$(GOARCH); go build -v -o $(RELEASE_DIR)/$(BINARY)-$(GOOS)-$(GOARCH))))

.PHONY: clean
clean:
	rm -rf $(RELEASE_DIR) || true