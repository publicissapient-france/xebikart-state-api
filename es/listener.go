package es

type EventListener interface {
	receive(message string)
}
