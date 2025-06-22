@file:OptIn(UnsafeDuringIrConstructionAPI::class)

package com.monkopedia.otli.codegen

import com.monkopedia.otli.builders.CodeBuilder
import com.monkopedia.otli.builders.Empty
import com.monkopedia.otli.builders.FileSymbol
import com.monkopedia.otli.builders.GroupSymbol
import com.monkopedia.otli.builders.LocalVar
import com.monkopedia.otli.builders.Raw
import com.monkopedia.otli.builders.Reference
import com.monkopedia.otli.builders.Return
import com.monkopedia.otli.builders.Symbol
import com.monkopedia.otli.builders.block
import com.monkopedia.otli.builders.op
import com.monkopedia.otli.builders.reference
import com.monkopedia.otli.builders.scopeBlock
import kotlin.math.exp
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrAnonymousInitializer
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrDeclarationWithName
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrErrorDeclaration
import org.jetbrains.kotlin.ir.declarations.IrExternalPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrLocalDelegatedProperty
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.IrPackageFragment
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrReplSnippet
import org.jetbrains.kotlin.ir.declarations.IrScript
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeAlias
import org.jetbrains.kotlin.ir.declarations.IrTypeParameter
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrBlockBody
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrBranch
import org.jetbrains.kotlin.ir.expressions.IrBreak
import org.jetbrains.kotlin.ir.expressions.IrBreakContinue
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrCallableReference
import org.jetbrains.kotlin.ir.expressions.IrCatch
import org.jetbrains.kotlin.ir.expressions.IrClassReference
import org.jetbrains.kotlin.ir.expressions.IrComposite
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstantArray
import org.jetbrains.kotlin.ir.expressions.IrConstantObject
import org.jetbrains.kotlin.ir.expressions.IrConstantPrimitive
import org.jetbrains.kotlin.ir.expressions.IrConstantValue
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrContainerExpression
import org.jetbrains.kotlin.ir.expressions.IrContinue
import org.jetbrains.kotlin.ir.expressions.IrDeclarationReference
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrDoWhileLoop
import org.jetbrains.kotlin.ir.expressions.IrDynamicExpression
import org.jetbrains.kotlin.ir.expressions.IrDynamicMemberExpression
import org.jetbrains.kotlin.ir.expressions.IrDynamicOperatorExpression
import org.jetbrains.kotlin.ir.expressions.IrElseBranch
import org.jetbrains.kotlin.ir.expressions.IrEnumConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrErrorCallExpression
import org.jetbrains.kotlin.ir.expressions.IrErrorExpression
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrExpressionBody
import org.jetbrains.kotlin.ir.expressions.IrFieldAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrGetClass
import org.jetbrains.kotlin.ir.expressions.IrGetEnumValue
import org.jetbrains.kotlin.ir.expressions.IrGetField
import org.jetbrains.kotlin.ir.expressions.IrGetObjectValue
import org.jetbrains.kotlin.ir.expressions.IrGetSingletonValue
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrInlinedFunctionBlock
import org.jetbrains.kotlin.ir.expressions.IrInstanceInitializerCall
import org.jetbrains.kotlin.ir.expressions.IrLocalDelegatedPropertyReference
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrPropertyReference
import org.jetbrains.kotlin.ir.expressions.IrRawFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.IrRichFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrRichPropertyReference
import org.jetbrains.kotlin.ir.expressions.IrSetField
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrStringConcatenation
import org.jetbrains.kotlin.ir.expressions.IrSuspendableExpression
import org.jetbrains.kotlin.ir.expressions.IrSuspensionPoint
import org.jetbrains.kotlin.ir.expressions.IrSyntheticBody
import org.jetbrains.kotlin.ir.expressions.IrThrow
import org.jetbrains.kotlin.ir.expressions.IrTry
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.IrValueAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.expressions.IrWhileLoop
import org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.visitors.IrVisitor

class CodegenVisitor : IrVisitor<Symbol, CodeBuilder>() {
    val declarationLookup = mutableMapOf<IrDeclarationWithName, LocalVar>()
    var currentFile: IrFile? = null
    var currentFunction: IrFunction? = null
    val testClasses = mutableListOf<IrClass>()

    override fun visitElement(element: IrElement, data: CodeBuilder): Symbol {
        error("Unsupported element type: ${element::class.simpleName}")
    }

    override fun visitDeclaration(declaration: IrDeclarationBase, data: CodeBuilder): Symbol =
        visitElement(declaration, data)

    override fun visitValueParameter(declaration: IrValueParameter, data: CodeBuilder): Symbol =
        visitDeclaration(declaration, data)

    override fun visitClass(declaration: IrClass, data: CodeBuilder): Symbol =
        if (declaration.isData) {
            buildStruct(declaration, data)
        } else {
            if (declaration.declarations.all { it.hasTestDeclaration }) {
                buildTest(declaration, data).also {
                    testClasses.add(declaration)
                }
            } else {
                visitDeclaration(declaration, data)
            }
        }

    override fun visitAnonymousInitializer(
        declaration: IrAnonymousInitializer,
        data: CodeBuilder
    ): Symbol = visitDeclaration(declaration, data)

    override fun visitTypeParameter(declaration: IrTypeParameter, data: CodeBuilder): Symbol =
        visitDeclaration(declaration, data)

    override fun visitFunction(declaration: IrFunction, data: CodeBuilder): Symbol {
        val lastFunction = currentFunction
        currentFunction = declaration
        return buildFunction(declaration, data).also {
            currentFunction = lastFunction
        }
    }

    override fun visitConstructor(declaration: IrConstructor, data: CodeBuilder): Symbol =
        visitFunction(declaration, data)

    override fun visitEnumEntry(declaration: IrEnumEntry, data: CodeBuilder): Symbol =
        visitDeclaration(declaration, data)

    override fun visitErrorDeclaration(
        declaration: IrErrorDeclaration,
        data: CodeBuilder
    ): Symbol = visitDeclaration(declaration, data)

    override fun visitField(declaration: IrField, data: CodeBuilder): Symbol =
        visitDeclaration(declaration, data)

    override fun visitLocalDelegatedProperty(
        declaration: IrLocalDelegatedProperty,
        data: CodeBuilder
    ): Symbol = visitDeclaration(declaration, data)

    override fun visitModuleFragment(declaration: IrModuleFragment, data: CodeBuilder): Symbol =
        GroupSymbol().apply {
            symbolList.addAll(declaration.files.map { it.accept(this@CodegenVisitor, data) })
            if (testClasses.isNotEmpty()) {
                symbolList.add(buildTestMain(testClasses, data))
            }
        }

    override fun visitProperty(declaration: IrProperty, data: CodeBuilder): Symbol {
        val initializer = declaration.backingField?.initializer?.accept(this@CodegenVisitor, data)
        return buildProperty(
            declaration,
            data,
            declaration.backingField?.type ?: error("Properties must have backing fields"),
            initializer
        )
    }

    override fun visitVariable(declaration: IrVariable, data: CodeBuilder): Symbol {
        val initializer = declaration.initializer?.accept(this@CodegenVisitor, data)
        return buildProperty(declaration, data, declaration.type, initializer)
    }

    override fun visitScript(declaration: IrScript, data: CodeBuilder): Symbol =
        visitDeclaration(declaration, data)

    override fun visitReplSnippet(declaration: IrReplSnippet, data: CodeBuilder): Symbol =
        visitDeclaration(declaration, data)

    override fun visitSimpleFunction(declaration: IrSimpleFunction, data: CodeBuilder): Symbol =
        visitFunction(declaration, data)

    override fun visitTypeAlias(declaration: IrTypeAlias, data: CodeBuilder): Symbol =
        visitDeclaration(declaration, data)

    override fun visitPackageFragment(
        declaration: IrPackageFragment,
        data: CodeBuilder
    ): Symbol = visitElement(declaration, data)

    override fun visitExternalPackageFragment(
        declaration: IrExternalPackageFragment,
        data: CodeBuilder
    ): Symbol = visitPackageFragment(declaration, data)

    @OptIn(UnsafeDuringIrConstructionAPI::class)
    override fun visitFile(declaration: IrFile, data: CodeBuilder): Symbol  {
        declaration.accept(HeaderVisitor(), data)
        return FileSymbol(data, declaration.packageFqName.pkgPrefix() + declaration.name + ".c").apply {
            val lastFile = currentFile
            currentFile = declaration
            groupSymbol.symbolList.addAll(
                declaration.declarations.map {
                    it.accept(this@CodegenVisitor, this)
                }
            )
            currentFile = lastFile
        }
    }

    override fun visitExpression(expression: IrExpression, data: CodeBuilder): Symbol =
        visitElement(expression, data)

    override fun visitBody(body: IrBody, data: CodeBuilder): Symbol = visitElement(body, data)

    override fun visitExpressionBody(body: IrExpressionBody, data: CodeBuilder): Symbol =
        body.expression.accept(this@CodegenVisitor, data)

    override fun visitBlockBody(body: IrBlockBody, data: CodeBuilder): Symbol =
        block(data, Empty) {
            body.statements.forEach { addSymbol(it.accept(this@CodegenVisitor, data)) }
        }

    override fun visitDeclarationReference(
        expression: IrDeclarationReference,
        data: CodeBuilder
    ): Symbol = declarationLookup[expression.symbol.owner]?.reference
        ?: error("Declaration not found")

    override fun visitMemberAccess(
        expression: IrMemberAccessExpression<*>,
        data: CodeBuilder
    ): Symbol = error("Unsupported")

    override fun visitFunctionAccess(
        expression: IrFunctionAccessExpression,
        data: CodeBuilder
    ): Symbol = visitMemberAccess(expression, data)

    override fun visitConstructorCall(expression: IrConstructorCall, data: CodeBuilder): Symbol =
        buildConstructor(expression, data)

    override fun visitSingletonReference(
        expression: IrGetSingletonValue,
        data: CodeBuilder
    ): Symbol = visitDeclarationReference(expression, data)

    override fun visitGetObjectValue(expression: IrGetObjectValue, data: CodeBuilder): Symbol =
        visitSingletonReference(expression, data)

    override fun visitGetEnumValue(expression: IrGetEnumValue, data: CodeBuilder): Symbol =
        visitSingletonReference(expression, data)

    override fun visitRawFunctionReference(
        expression: IrRawFunctionReference,
        data: CodeBuilder
    ): Symbol = visitDeclarationReference(expression, data)

    override fun visitContainerExpression(
        expression: IrContainerExpression,
        data: CodeBuilder
    ): Symbol = visitExpression(expression, data)

    override fun visitBlock(expression: IrBlock, data: CodeBuilder): Symbol =
        GroupSymbol().apply {
            expression.statements.forEach {
                symbolList.add(it.accept(this@CodegenVisitor, data))
            }
        }

    override fun visitComposite(expression: IrComposite, data: CodeBuilder): Symbol =
        visitContainerExpression(expression, data)

    override fun visitReturnableBlock(expression: IrReturnableBlock, data: CodeBuilder): Symbol =
        visitBlock(expression, data)

    override fun visitInlinedFunctionBlock(
        inlinedBlock: IrInlinedFunctionBlock,
        data: CodeBuilder
    ): Symbol = visitBlock(inlinedBlock, data)

    override fun visitSyntheticBody(body: IrSyntheticBody, data: CodeBuilder): Symbol =
        visitBody(body, data)

    override fun visitBreakContinue(jump: IrBreakContinue, data: CodeBuilder): Symbol =
        visitExpression(jump, data)

    override fun visitBreak(jump: IrBreak, data: CodeBuilder): Symbol =
        visitBreakContinue(jump, data)

    override fun visitContinue(jump: IrContinue, data: CodeBuilder): Symbol =
        visitBreakContinue(jump, data)

    override fun visitCall(expression: IrCall, data: CodeBuilder): Symbol =
        buildCall(expression, data)

    override fun visitCallableReference(
        expression: IrCallableReference<*>,
        data: CodeBuilder
    ): Symbol = visitMemberAccess(expression, data)

    override fun visitFunctionReference(
        expression: IrFunctionReference,
        data: CodeBuilder
    ): Symbol = visitCallableReference(expression, data)

    override fun visitPropertyReference(
        expression: IrPropertyReference,
        data: CodeBuilder
    ): Symbol = visitCallableReference(expression, data)

    override fun visitLocalDelegatedPropertyReference(
        expression: IrLocalDelegatedPropertyReference,
        data: CodeBuilder
    ): Symbol = visitCallableReference(expression, data)

    override fun visitRichFunctionReference(
        expression: IrRichFunctionReference,
        data: CodeBuilder
    ): Symbol = visitExpression(expression, data)

    override fun visitRichPropertyReference(
        expression: IrRichPropertyReference,
        data: CodeBuilder
    ): Symbol = visitExpression(expression, data)

    override fun visitClassReference(expression: IrClassReference, data: CodeBuilder): Symbol =
        visitDeclarationReference(expression, data)

    override fun visitConst(expression: IrConst, data: CodeBuilder): Symbol =
        Raw(expression.value.toString())

    override fun visitConstantValue(expression: IrConstantValue, data: CodeBuilder): Symbol =
        visitExpression(expression, data)

    override fun visitConstantPrimitive(
        expression: IrConstantPrimitive,
        data: CodeBuilder
    ): Symbol = visitConstantValue(expression, data)

    override fun visitConstantObject(expression: IrConstantObject, data: CodeBuilder): Symbol =
        visitConstantValue(expression, data)

    override fun visitConstantArray(expression: IrConstantArray, data: CodeBuilder): Symbol =
        visitConstantValue(expression, data)

    override fun visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall,
        data: CodeBuilder
    ): Symbol = visitFunctionAccess(expression, data)

    override fun visitDynamicExpression(
        expression: IrDynamicExpression,
        data: CodeBuilder
    ): Symbol = visitExpression(expression, data)

    override fun visitDynamicOperatorExpression(
        expression: IrDynamicOperatorExpression,
        data: CodeBuilder
    ): Symbol = visitDynamicExpression(expression, data)

    override fun visitDynamicMemberExpression(
        expression: IrDynamicMemberExpression,
        data: CodeBuilder
    ): Symbol = visitDynamicExpression(expression, data)

    override fun visitEnumConstructorCall(
        expression: IrEnumConstructorCall,
        data: CodeBuilder
    ): Symbol = visitFunctionAccess(expression, data)

    override fun visitErrorExpression(expression: IrErrorExpression, data: CodeBuilder): Symbol =
        visitExpression(expression, data)

    override fun visitErrorCallExpression(
        expression: IrErrorCallExpression,
        data: CodeBuilder
    ): Symbol = visitErrorExpression(expression, data)

    override fun visitFieldAccess(
        expression: IrFieldAccessExpression,
        data: CodeBuilder
    ): Symbol = visitDeclarationReference(expression, data)

    override fun visitGetField(expression: IrGetField, data: CodeBuilder): Symbol =
        visitFieldAccess(expression, data)

    override fun visitSetField(expression: IrSetField, data: CodeBuilder): Symbol =
        visitFieldAccess(expression, data)

    override fun visitFunctionExpression(
        expression: IrFunctionExpression,
        data: CodeBuilder
    ): Symbol =
        if (expression.function.returnType.isUnit()) {
            data.scopeBlock {
                +(expression.function.body?.accept(this@CodegenVisitor, this)
                    ?: error("Function is missing the body"))
            }
        } else {
            visitExpression(expression, data)
        }

    override fun visitGetClass(expression: IrGetClass, data: CodeBuilder): Symbol =
        visitExpression(expression, data)

    override fun visitInstanceInitializerCall(
        expression: IrInstanceInitializerCall,
        data: CodeBuilder
    ): Symbol = visitExpression(expression, data)

    override fun visitLoop(loop: IrLoop, data: CodeBuilder): Symbol = visitExpression(loop, data)

    override fun visitWhileLoop(loop: IrWhileLoop, data: CodeBuilder): Symbol =
        buildLoop(loop, data)

    override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: CodeBuilder): Symbol =
        visitLoop(loop, data)

    override fun visitReturn(expression: IrReturn, data: CodeBuilder): Symbol =
        Return(expression.value.accept(this@CodegenVisitor, data))

    override fun visitStringConcatenation(
        expression: IrStringConcatenation,
        data: CodeBuilder
    ): Symbol = visitExpression(expression, data)

    override fun visitSuspensionPoint(expression: IrSuspensionPoint, data: CodeBuilder): Symbol =
        visitExpression(expression, data)

    override fun visitSuspendableExpression(
        expression: IrSuspendableExpression,
        data: CodeBuilder
    ): Symbol = visitExpression(expression, data)

    override fun visitThrow(expression: IrThrow, data: CodeBuilder): Symbol =
        visitExpression(expression, data)

    override fun visitTry(aTry: IrTry, data: CodeBuilder): Symbol = visitExpression(aTry, data)

    override fun visitCatch(aCatch: IrCatch, data: CodeBuilder): Symbol =
        visitElement(aCatch, data)

    override fun visitTypeOperator(expression: IrTypeOperatorCall, data: CodeBuilder): Symbol =
        if (expression.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
            expression.argument.accept(this, data)
        } else {
            error("Unsupported opperator ${expression.argument}")
        }

    override fun visitValueAccess(
        expression: IrValueAccessExpression,
        data: CodeBuilder
    ): Symbol =
        declarationLookup[expression.symbol.owner]?.reference
            ?: error("$expression has not been mapped")

    override fun visitGetValue(expression: IrGetValue, data: CodeBuilder): Symbol =
        visitValueAccess(expression, data)

    override fun visitSetValue(expression: IrSetValue, data: CodeBuilder): Symbol =
        // TOD: Require not iterator
        visitValueAccess(expression, data).op("=", expression.value.accept(this, data))

    override fun visitVararg(expression: IrVararg, data: CodeBuilder): Symbol =
        visitExpression(expression, data)

    override fun visitSpreadElement(spread: IrSpreadElement, data: CodeBuilder): Symbol =
        visitElement(spread, data)

    override fun visitWhen(expression: IrWhen, data: CodeBuilder): Symbol =
        visitExpression(expression, data)

    override fun visitBranch(branch: IrBranch, data: CodeBuilder): Symbol =
        visitElement(branch, data)

    override fun visitElseBranch(branch: IrElseBranch, data: CodeBuilder): Symbol =
        visitBranch(branch, data)
}
