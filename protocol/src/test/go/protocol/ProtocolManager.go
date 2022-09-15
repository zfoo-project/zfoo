package protocol

func init() {
	Protocols[101] = new(NormalObject)
	Protocols[102] = new(ObjectA)
	Protocols[103] = new(ObjectB)
	Protocols[104] = new(SimpleObject)
}
