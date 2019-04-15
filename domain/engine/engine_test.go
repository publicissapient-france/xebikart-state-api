package engine_test

import (
	"testing"

	. "github.com/onsi/ginkgo"
	. "github.com/onsi/gomega"
	. "github.com/xebia-france/xebikart-state-api/domain/engine"
)

func TestCli(t *testing.T) {
	RegisterFailHandler(Fail)
	RunSpecs(t, "Xebikart Domain Engine Suite")
}

var _ = Describe("Account aggregate", func() {

	Context("With initial account acitivated", func() {
		var accountAggregate = Account{}
		var accountInitialState = AccountActivated{amount: 100}

		It("should be increased amount with command credited", func() {
			var command = CreditAmount{amount: 200}
			events := accountAggregate.Decide(accountInitialState, command)

			Expect(events).To(HaveLen(1))
			event := events[0].(AccountCredited)
			Expect(event.amount).To(Equal(200))

			newState := accountAggregate.Apply(accountInitialState, event)

			Expect(newState).ShouldNot(BeNil())
			Expect(newState.(AccountActivated).amount).Should(Equal(300))
		})

		It("should be rehydrated from a list of event", func() {
			events := []Event{
				AccountCredited{amount: 200},
				AccountCredited{amount: 150},
			}
			state, version := Replay(accountAggregate, accountInitialState, events)
			Expect(version).Should(Equal(2))
			Expect(state).Should(BeAssignableToTypeOf(AccountActivated{amount: 0}))
			var actual = state.(AccountActivated)
			Expect(actual.amount).Should(Equal(450))
		})
	})
})

type AccountActivated struct {
	amount int
}

type AccountCredited struct {
	amount int
}

type CreditAmount struct {
	amount int
}

type Account struct{}

func (this Account) Decide(state State, command Command) []Event {
	_, isAccountActivated := state.(AccountActivated)
	_, isCreditAmount := command.(CreditAmount)
	if isCreditAmount && isAccountActivated {
		return []Event{AccountCredited{amount: command.(CreditAmount).amount}}
	}
	return []Event{}
}

func (this Account) Apply(state State, event Event) State {
	_, isAccountActivated := state.(AccountActivated)
	_, isAccountCredited := event.(AccountCredited)
	if isAccountActivated && isAccountCredited {
		return AccountActivated{amount: state.(AccountActivated).amount + event.(AccountCredited).amount}
	}
	return nil
}
