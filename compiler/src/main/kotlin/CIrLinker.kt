package com.monkopedia.kot

import org.jetbrains.kotlin.backend.common.linkage.partial.PartialLinkageSupportForLinker
import org.jetbrains.kotlin.backend.common.overrides.IrLinkerFakeOverrideProvider
import org.jetbrains.kotlin.backend.common.serialization.BasicIrModuleDeserializer
import org.jetbrains.kotlin.backend.common.serialization.CurrentModuleWithICDeserializer
import org.jetbrains.kotlin.backend.common.serialization.DeserializationStrategy
import org.jetbrains.kotlin.backend.common.serialization.ICData
import org.jetbrains.kotlin.backend.common.serialization.IrModuleDeserializer
import org.jetbrains.kotlin.backend.common.serialization.KotlinIrLinker
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.types.IrTypeSystemContextImpl
import org.jetbrains.kotlin.ir.util.DeclarationStubGenerator
import org.jetbrains.kotlin.ir.util.IdSignature
import org.jetbrains.kotlin.ir.util.SymbolTable
import org.jetbrains.kotlin.library.IrLibrary
import org.jetbrains.kotlin.library.KotlinAbiVersion
import org.jetbrains.kotlin.library.KotlinLibrary
import org.jetbrains.kotlin.library.containsErrorCode
import org.jetbrains.kotlin.utils.memoryOptimizedMap

class CIrLinker(
    private val currentModule: ModuleDescriptor?,
    messageCollector: MessageCollector,
    builtIns: IrBuiltIns,
    symbolTable: SymbolTable,
    override val partialLinkageSupport: PartialLinkageSupportForLinker,
    private val icData: ICData? = null,
    friendModules: Map<String, Collection<String>> = emptyMap(),
    private val stubGenerator: DeclarationStubGenerator? = null
) : KotlinIrLinker(
    currentModule = currentModule,
    messageCollector = messageCollector,
    builtIns = builtIns,
    symbolTable = symbolTable,
    exportedDependencies = emptyList(),
    symbolProcessor = { symbol, idSig ->
        if (idSig.isLocal) {
            symbol.privateSignature =
                IdSignature.CompositeSignature(IdSignature.FileSignature(fileSymbol), idSig)
        }
        symbol
    }
) {
    override val fakeOverrideBuilder =
        IrLinkerFakeOverrideProvider(
            linker = this,
            symbolTable = symbolTable,
            mangler = CManglerIr,
            typeSystem = IrTypeSystemContextImpl(builtIns),
            friendModules = friendModules,
            partialLinkageSupport = partialLinkageSupport
        )

    override fun isBuiltInModule(moduleDescriptor: ModuleDescriptor): Boolean =
        moduleDescriptor === moduleDescriptor.builtIns.builtInsModule

    private val IrLibrary.libContainsErrorCode: Boolean
        get() = this is KotlinLibrary && this.containsErrorCode

    override fun createModuleDeserializer(
        moduleDescriptor: ModuleDescriptor,
        klib: KotlinLibrary?,
        strategyResolver: (String) -> DeserializationStrategy
    ): IrModuleDeserializer {
        require(klib != null) { "Expecting kotlin library" }
        val libraryAbiVersion = klib.versions.abiVersion ?: KotlinAbiVersion.CURRENT
        return when (val lazyIrGenerator = stubGenerator) {
            null ->
                CModuleDeserializer(
                    moduleDescriptor,
                    klib,
                    strategyResolver,
                    libraryAbiVersion,
                    klib.libContainsErrorCode
                )

            else ->
                CLazyIrModuleDeserializer(
                    moduleDescriptor,
                    libraryAbiVersion,
                    lazyIrGenerator
                )
        }
    }

    private val deserializedFilesInKlibOrder = mutableMapOf<IrModuleFragment, List<IrFile>>()

    private inner class CModuleDeserializer(
        moduleDescriptor: ModuleDescriptor,
        klib: IrLibrary,
        strategyResolver: (String) -> DeserializationStrategy,
        libraryAbiVersion: KotlinAbiVersion,
        allowErrorCode: Boolean
    ) : BasicIrModuleDeserializer(
        this,
        moduleDescriptor,
        klib,
        strategyResolver,
        libraryAbiVersion,
        allowErrorCode,
        true
    ) {
        override fun init(delegate: IrModuleDeserializer) {
            super.init(delegate)
            deserializedFilesInKlibOrder[moduleFragment] =
                fileDeserializationStates.memoryOptimizedMap { it.file }
        }
    }

    override fun createCurrentModuleDeserializer(
        moduleFragment: IrModuleFragment,
        dependencies: Collection<IrModuleDeserializer>
    ): IrModuleDeserializer {
        val currentModuleDeserializer =
            super.createCurrentModuleDeserializer(moduleFragment, dependencies)

        icData?.let {
            return CurrentModuleWithICDeserializer(
                currentModuleDeserializer,
                symbolTable,
                builtIns,
                it.icData
            ) { lib ->
                CModuleDeserializer(
                    currentModuleDeserializer.moduleDescriptor,
                    lib,
                    currentModuleDeserializer.strategyResolver,
                    KotlinAbiVersion.CURRENT,
                    it.containsErrorCode
                )
            }
        }
        return currentModuleDeserializer
    }

    val modules
        get() = deserializersForModules.values
            .map { it.moduleFragment }
            .filter { it.descriptor !== currentModule }
}
