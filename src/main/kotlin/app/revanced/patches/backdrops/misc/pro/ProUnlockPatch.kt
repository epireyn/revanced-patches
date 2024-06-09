package app.revanced.patches.backdrops.misc.pro

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.backdrops.misc.pro.fingerprints.proUnlockFingerprint
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction

@Suppress("unused")
val proUnlockPatch = bytecodePatch(
    name = "Pro unlock",
) {
    compatibleWith("com.backdrops.wallpapers"("4.52"))

    val proUnlockResult by proUnlockFingerprint

    execute {
        val registerIndex = proUnlockResult.scanResult.patternScanResult!!.endIndex - 1

        proUnlockResult.mutableMethod.apply {
            val register = getInstruction<OneRegisterInstruction>(registerIndex).registerA
            addInstruction(
                proUnlockResult.scanResult.patternScanResult!!.endIndex,
                "const/4 v$register, 0x1",
            )
        }
    }
}
