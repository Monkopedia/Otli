package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.BlockSymbol
import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.Empty
import com.monkopedia.otli.builders.Goto
import com.monkopedia.otli.builders.GotoTarget
import com.monkopedia.otli.builders.GroupSymbol
import com.monkopedia.otli.builders.IfBuilder
import com.monkopedia.otli.builders.ResolvedType
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.assign
import com.monkopedia.otli.builders.buildIf
import com.monkopedia.otli.builders.define
import com.monkopedia.otli.builders.reference
import com.monkopedia.otli.builders.scope
import com.monkopedia.otli.builders.scopeBlock
import com.monkopedia.otli.type.WrappedType
import org.jetbrains.kotlin.ir.expressions.IrBranch
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrWhen

fun CodegenVisitor.buildWhen(irWhen: IrWhen, data: CodeBuilder): Symbol {
    val type = ResolvedType(irWhen.type)
    val whenRet = if (type != WrappedType("void")) {
        data.define(data, "whenRet", type).also {
            data.addSymbol(it)
        }
    } else {
        null
    }
    val branchesWithConditions = irWhen.branches.map {
        it to if (it is IrElseBranch) {
            null
        } else {
            data.scopeBlock(false) {
                it.condition.accept(
                    this@buildWhen,
                    this@scopeBlock
                ).let(::add)
            }
        }
    }

    if (branchesWithConditions.drop(1).all {
            it.second == null || (it.second as? GroupSymbol)?.symbols?.size == 1
        }
    ) {
        val iterator = branchesWithConditions.iterator()
        fun IfBuilder.next(branch: IrBranch, condition: Symbol?) {
            this.condition = when (condition) {
                is GroupSymbol -> {
                    condition.symbols.single()
                }

                is BlockSymbol -> condition.mutateSymbols {
                    dropLast(1).forEach { data.add(it) }
                    removeLast()
                }

                else -> condition
            }
            ifBlock {
                val result = branch.result.accept(this@buildWhen, this@ifBlock)
                add(whenRet?.reference?.assign(result) ?: result)
            }
            if (iterator.hasNext()) {
                val (branch, condition) = iterator.next()
                if (branch is IrElseBranch) {
                    elseBlock {
                        val result = branch.result.accept(this@buildWhen, this@elseBlock)
                        add(whenRet?.reference?.assign(result) ?: result)
                    }
                } else {
                    elseIfBlock {
                        this@elseIfBlock.next(branch, condition)
                    }
                }
            }
        }
        val (branch, condition) = iterator.next()
        val ifSymbol = data.scopeBlock(false) {
            add(buildIf { next(branch, condition) })
        }
        data.addSymbol(ifSymbol)
    } else {
        val gotoTarget = GotoTarget(data.scope.allocateName("when_goto", data))
        for (branch in irWhen.branches) {
            if (branch !is IrElseBranch) {
                data.addSymbol(
                    data.scopeBlock(false) {
                        add(
                            buildIf {
                                condition =
                                    branch.condition.accept(this@buildWhen, this@scopeBlock)
                                ifBlock {
                                    val result = branch.result.accept(
                                        this@buildWhen,
                                        this@ifBlock
                                    )
                                    add(whenRet?.reference?.assign(result) ?: result)
                                    add(Goto(gotoTarget))
                                }
                            }
                        )
                    }
                )
            } else {
                data.addSymbol(
                    data.scopeBlock(false) {
                        val result = branch.result.accept(
                            this@buildWhen,
                            this@scopeBlock
                        )
                        add(whenRet?.reference?.assign(result) ?: result)
                        add(Goto(gotoTarget))
                    }
                )
            }
        }

        data.addSymbol(gotoTarget)
    }
    return whenRet?.reference ?: Empty
}
