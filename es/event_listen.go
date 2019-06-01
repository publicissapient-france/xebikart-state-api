package es

import (
	"fmt"

	"github.com/streadway/amqp"
)

type AmqpListener struct {
	username      string
	password      string
	host          string
	port          int
	channel       string
	eventListener EventListener
}

func (t *AmqpListener) Listen() {
	conn, _ := amqp.Dial(t.provideAmqConnectionString())

	defer conn.Close()
}

func (t *AmqpListener) provideAmqConnectionString() string {
	return fmt.Sprintf(
		"amqp://%s:%s@%s:%d/",
		t.username,
		t.password,
		t.host,
		t.port,
	)
}
