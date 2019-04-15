package engine

type State interface{}

type Event interface{}

type Command interface{}

type Aggregate interface {
	Decide(state State, command Command) []Event
	Apply(state State, event Event) State
}

func Replay(aggregate Aggregate, initialState State, events []Event) (State, int) {
	var currentState = initialState
	var version int = 0
	for _, event := range events {
		version++
		currentState = aggregate.Apply(currentState, event)
	}
	return currentState, version
}
