describe("Hello World", () => {

    it("should always pass", () => {
        expect(true).toBe(true)
    })

    it("should return hello world", () => {
        let hello = "Hello World!"
        expect(helloworld()).toBe(hello)
    })
})