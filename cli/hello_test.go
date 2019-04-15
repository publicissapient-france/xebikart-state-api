package cli

import (
	"testing"

	. "github.com/onsi/gomega"
)

func TestSum(t *testing.T) {
	g := NewGomegaWithT(t)

	actual := SumHello(1, 2)

	g.Expect(actual).To(Equal(3))

}
