package com.monkopedia.kot

import java.lang.StringBuilder
import org.jetbrains.kotlin.backend.common.serialization.mangle.KotlinExportChecker
import org.jetbrains.kotlin.backend.common.serialization.mangle.KotlinMangleComputer
import org.jetbrains.kotlin.backend.common.serialization.mangle.MangleMode
import org.jetbrains.kotlin.backend.common.serialization.mangle.descriptor.DescriptorBasedKotlinManglerImpl
import org.jetbrains.kotlin.backend.common.serialization.mangle.descriptor.DescriptorExportCheckerVisitor
import org.jetbrains.kotlin.backend.common.serialization.mangle.descriptor.DescriptorMangleComputer
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrBasedKotlinManglerImpl
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrExportCheckerVisitor
import org.jetbrains.kotlin.backend.common.serialization.mangle.ir.IrMangleComputer
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration

abstract class AbstractCManglerIr : IrBasedKotlinManglerImpl() {
    private class CIrExportChecker(compatibleMode: Boolean) :
        IrExportCheckerVisitor(compatibleMode) {
        override fun IrDeclaration.isPlatformSpecificExported() = false
    }

    override fun getExportChecker(compatibleMode: Boolean): KotlinExportChecker<IrDeclaration> =
        CIrExportChecker(compatibleMode)

    override fun getMangleComputer(
        mode: MangleMode,
        compatibleMode: Boolean
    ): KotlinMangleComputer<IrDeclaration> =
        IrMangleComputer(StringBuilder(256), mode, compatibleMode)
}

object CManglerIr : AbstractCManglerIr()

abstract class AbstractCDescriptorMangler : DescriptorBasedKotlinManglerImpl() {
    companion object {
        private val exportChecker = CDescriptorExportChecker()
    }

    private class CDescriptorExportChecker : DescriptorExportCheckerVisitor() {
        override fun DeclarationDescriptor.isPlatformSpecificExported() = false
    }

    override fun getExportChecker(
        compatibleMode: Boolean
    ): KotlinExportChecker<DeclarationDescriptor> = exportChecker

    override fun getMangleComputer(
        mode: MangleMode,
        compatibleMode: Boolean
    ): KotlinMangleComputer<DeclarationDescriptor> =
        DescriptorMangleComputer(StringBuilder(256), mode)
}

object CManglerDesc : AbstractCDescriptorMangler()
