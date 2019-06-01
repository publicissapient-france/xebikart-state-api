package main

import (
	"fmt"
	"log"
	"net/http"
	"time"

	"github.com/xebia-france/xebikart-state-api/es"
	"github.com/xebia-france/xebikart-state-api/sse"
)

func hello(rw http.ResponseWriter, req *http.Request) {
	flusher, _ := rw.(http.Flusher)

	rw.Header().Set("Cache-Control", "no-cache")
	rw.Header().Set("Access-Control-Allow-Origin", "*")
	fmt.Fprintf(rw, "OK")
	flusher.Flush()
}

func main() {

	broker := sse.NewBroker()
	externalEventListener := &es.AmqpListener{
		EventListener: &es.SSEBridgeEventListner{
			NotifierChannel: broker.Notifier,
		},
	}

	go externalEventListener.Listen()

	go func() {
		for {
			time.Sleep(time.Second * 2)
			eventString := fmt.Sprintf("{\"race\": {\"state\": \"AWAITING\"}}")
			log.Println("Receiving event")
			broker.Notifier <- []byte(eventString)
		}
	}()

	http.HandleFunc("/events", broker.SSEHandler)
	http.HandleFunc("/", hello)

	log.Fatal("HTTP server error: ", http.ListenAndServe("0.0.0.0:80", nil))

}
