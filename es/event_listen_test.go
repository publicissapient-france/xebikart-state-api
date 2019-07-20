package es

import (
	"testing"

	. "github.com/onsi/ginkgo"
	. "github.com/onsi/gomega"
)

func TestCli(t *testing.T) {
	RegisterFailHandler(Fail)
	RunSpecs(t, "Xebikart Event Source test suite")
}

var _ = Describe("Amqp broker", func() {

	Context("which send an event", func() {

		It("should be sent to the listener", func() {

		})

	})
})

var _ = Describe("Disconnected Event listener", func() {
	var amqpListener = AmqpListener{
		Host:     "fakehost",
		Port:     5566,
		Username: "xebi",
		Password: "kart",
	}
	Context("which request to connect and listen", func() {

		It("should generate a valid Amqp connection url", func() {
			connectionString := amqpListener.provideAmqConnectionString()
			Expect(connectionString).To(Equal("amqp://xebi:kart@fakehost:5566/"))
		})
	})
})
