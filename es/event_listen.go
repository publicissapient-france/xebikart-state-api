package es

import (
	"fmt"
	"log"

	"github.com/streadway/amqp"
)

type AmqpListener struct {
	Username      string
	Password      string
	Host          string
	Port          int
	QueueName     string
	EventListener EventListener
}

func (t *AmqpListener) Listen() {
	//	Inspirated by https://www.rabbitmq.com/tutorials/tutorial-one-go.html
	conn, _ := amqp.Dial(t.provideAmqConnectionString())
	defer conn.Close()
	channel, _ := conn.Channel()
	defer channel.Close()

	q, _ := channel.QueueDeclare(
		t.QueueName, // name
		true,        // durable
		true,        // delete when usused
		false,       // exclusive
		false,       // no-wait
		nil,         // arguments
	)
	msgs, _ := channel.Consume(
		q.Name, // queue
		"",     // consumer
		true,   // auto-ack
		false,  // exclusive
		false,  // no-local
		false,  // no-wait
		nil,    // args
	)

	forever := make(chan bool)
	go func() {
		for d := range msgs {
			t.EventListener.receive(string(d.Body))
		}
	}()

	log.Printf(" [*] Waiting for messages. To exit press CTRL+C")
	<-forever

}

func (t *AmqpListener) provideAmqConnectionString() string {
	return fmt.Sprintf(
		"amqp://%s:%s@%s:%d/",
		t.Username,
		t.Password,
		t.Host,
		t.Port,
	)
}
