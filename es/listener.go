package es

type EventListener interface {
	receive(message string)
}

type SSEBridgeEventListner struct {
	NotifierChannel chan []byte
}

func (t *SSEBridgeEventListner) receive(message string) {
	t.NotifierChannel <- []byte(message)
}
