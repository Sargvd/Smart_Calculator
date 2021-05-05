package calculator

import java.lang.Exception
import java.math.BigInteger
import kotlin.math.pow

object Memory {
    val bank = mutableMapOf<String, BigInteger>()
}

class Stack {
    var s = mutableListOf<String>()
    fun push(s: String) {
        this.s.add(s)
    }
    fun pop(): String {
        if(this.isEmpty()) throw Exception("")
        val el = this.s[this.s.size - 1]
        this.s.removeAt(this.s.size - 1)
        return el
    }
    fun peek(): String {
        if(this.isEmpty()) return ""
        return this.s[this.s.size - 1]
    }

    fun isEmpty(): Boolean {
        return this.s.isEmpty()
    }
}

fun calculateString(str: String): BigInteger {
    val ops = str.replace("(", "( ").replace(")", " )").split(" ")

    //Assignment mode
    if ('=' in str) {
        val ops = str.split("=").map { it.trim() }
        if (ops.size > 2) throw Exception("Invalid assignment")
        if (!ops[0].matches("^[a-zA-Z]+$".toRegex())) throw Exception("Invalid identifier")
        if (!ops[1].matches("^-?[0-9]+$".toRegex())) {
            if (!Memory.bank.containsKey(ops[1])) throw Exception("Invalid assignment")
            else Memory.bank[ops[0]] = Memory.bank[ops[1]]!!
        } else Memory.bank[ops[0]] = ops[1].toBigInteger()
        return BigInteger.ZERO
    }

    //Expression mode
    val postfix = mutableListOf<String>()
    val postfixHelper = Stack()
    val calcStack = Stack()

    for (i in ops.indices) {
        var el = ops[i]

        if (el.matches("-?[0-9]+".toRegex())) {
            postfix.add(el)
            continue
        } else if (el.matches("[a-zA-Z]+".toRegex())) {
            if (Memory.bank.containsKey(el)) {
                postfix.add(Memory.bank[el].toString())
                continue
            } else throw Exception("Unknown variable")
        }

        if (el.matches("\\+{2,}".toRegex())) el = "+"
        else if (el.matches("-{2,}".toRegex())) {
            el = if ((el.length % 2 ) == 0) "+" else "-"
        } else if (el.matches("\\*{2,}|/{2,}".toRegex())) throw Exception("")

        if(postfixHelper.isEmpty() || postfixHelper.peek() == "(") postfixHelper.push(el)
        else if (el == "^" && postfixHelper.peek() in listOf("+","-","*","/")) postfixHelper.push(el)
        else if (el in listOf("*", "/") && postfixHelper.peek() in listOf("+", "-")) postfixHelper.push(el)
        else if (el in listOf("+", "-", "*", "/", "^")) {
            if (el == "^") {
                while (postfixHelper.peek() !in listOf("*", "/", "+", "-", "(", "")) postfix.add(postfixHelper.pop())
                postfixHelper.push(el)
            } else if (el in listOf("*", "/")) {
                while (postfixHelper.peek() !in listOf("+", "-", "(", "")) postfix.add(postfixHelper.pop())
                postfixHelper.push(el)
            } else {
                while (postfixHelper.peek() !in listOf("(", "")) postfix.add(postfixHelper.pop())
                postfixHelper.push(el)
            }
        } else if (el == "(") postfixHelper.push(el)
        else if (el == ")") {
            while (postfixHelper.peek() != "(") postfix.add(postfixHelper.pop())
            postfixHelper.pop()
        }
    }

    while (!postfixHelper.isEmpty()) {
        val el = postfixHelper.pop()
        if(el == "(") throw Exception("")
        postfix.add(el)
    }

    for (i in postfix.indices) {
        val el = postfix[i]
        if (el.matches("^-?[0-9]+$".toRegex())) calcStack.push(el)
        else if(el in listOf("+", "-", "*", "/", "^")) {
            val b = calcStack.pop().toBigInteger()
            val a = calcStack.pop().toBigInteger()
            when (el) {
                "+" -> calcStack.push((a + b).toString())
                "-" -> calcStack.push((a - b).toString())
                "*" -> calcStack.push((a * b).toString())
                "/" -> calcStack.push((a / b).toString())
                "^" -> calcStack.push(a.pow(b.toInt()).toInt().toString())
            }
        }
    }

    return calcStack.pop().toBigInteger()
}

fun main() {
    while(true) {
        val l = readLine()!!
        when {
            l == "/exit" -> {
                println("Bye!")
                break
            }
            l == "/help" -> println("The program calculates the sum of numbers")
            l.matches("^/.*".toRegex()) -> println("Unknown command")
            l == "" -> continue
            '=' in l -> {
                try { calculateString(l) }
                catch (e: Exception) {println(e.message)}
            }
            else -> {
                try { println(calculateString(l)) }
                catch (e: Exception) {
                    if (e.message != "") println(e.message)
                    else println("Invalid expression")
                }
            }
        }
    }
}
