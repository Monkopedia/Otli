package com.monkopedia.otli

import org.jetbrains.kotlin.backend.common.serialization.DescriptorByIdSignatureFinderImpl
import org.jetbrains.kotlin.backend.common.serialization.IrModuleDeserializer
import org.jetbrains.kotlin.backend.common.serialization.IrModuleDeserializerKind
import org.jetbrains.kotlin.backend.common.serialization.encodings.BinarySymbolData
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrField
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.declarations.impl.IrModuleFragmentImpl
import org.jetbrains.kotlin.ir.symbols.IrFieldSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.DeclarationStubGenerator
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.library.KotlinAbiVersion

class OtliLazyIrModuleDeserializer(
    moduleDescriptor: ModuleDescriptor,
    libraryAbiVersion: KotlinAbiVersion,
    private val stubGenerator: DeclarationStubGenerator
) : IrModuleDeserializer(moduleDescriptor, libraryAbiVersion) {
    private val dependencies = emptyList<IrModuleDeserializer>()

    // TODO: implement proper check whether `idSig` belongs to this module
    override fun contains(idSig: IdSignature): Boolean = true

    private val descriptorFinder = DescriptorByIdSignatureFinderImpl(moduleDescriptor, OtliManglerDesc)

    override fun tryDeserializeIrSymbol(
        idSig: IdSignature,
        symbolKind: BinarySymbolData.SymbolKind
    ): IrSymbol? {
        val descriptor = descriptorFinder.findDescriptorBySignature(idSig) ?: return null

        val declaration =
            stubGenerator.run {
                when (symbolKind) {
                    BinarySymbolData.SymbolKind.CLASS_SYMBOL ->
                        generateClassStub(descriptor as ClassDescriptor)

                    BinarySymbolData.SymbolKind.PROPERTY_SYMBOL ->
                        generatePropertyStub(descriptor as PropertyDescriptor)

                    BinarySymbolData.SymbolKind.FUNCTION_SYMBOL ->
                        generateFunctionStub(descriptor as FunctionDescriptor)

                    BinarySymbolData.SymbolKind.CONSTRUCTOR_SYMBOL ->
                        generateConstructorStub(descriptor as ClassConstructorDescriptor)

                    BinarySymbolData.SymbolKind.ENUM_ENTRY_SYMBOL ->
                        generateEnumEntryStub(descriptor as ClassDescriptor)

                    BinarySymbolData.SymbolKind.TYPEALIAS_SYMBOL ->
                        generateTypeAliasStub(descriptor as TypeAliasDescriptor)

                    else -> error("Unexpected type $symbolKind for sig $idSig")
                }
            }

        return declaration.symbol
    }

    override fun deserializedSymbolNotFound(idSig: IdSignature): Nothing =
        error("No descriptor found for $idSig")

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun declareIrSymbol(symbol: IrSymbol) {
        if (symbol is IrFieldSymbol) {
            declareFieldStub(symbol)
        } else {
            stubGenerator.generateMemberStub(symbol.descriptor)
        }
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    private fun declareFieldStub(symbol: IrFieldSymbol): IrField = with(stubGenerator) {
        val old = stubGenerator.unboundSymbolGeneration
        try {
            stubGenerator.unboundSymbolGeneration = true
            generateFieldStub(symbol.descriptor)
        } finally {
            stubGenerator.unboundSymbolGeneration = old
        }
    }

    override val moduleFragment: IrModuleFragment = IrModuleFragmentImpl(moduleDescriptor)
    override val moduleDependencies: Collection<IrModuleDeserializer> = dependencies

    override val kind get() = IrModuleDeserializerKind.SYNTHETIC
}
