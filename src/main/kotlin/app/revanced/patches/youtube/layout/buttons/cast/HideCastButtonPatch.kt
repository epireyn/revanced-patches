package app.revanced.patches.youtube.layout.buttons.cast

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.PatchException
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch

@Suppress("unused")
val hideCastButtonPatch = bytecodePatch(
    name = "Hide cast button",
    description = "Adds an option to hide the cast button in the video player.",
) {
    dependsOn(integrationsPatch, settingsPatch, addResourcesPatch)

    compatibleWith("com.google.android.youtube"())

    execute { context ->
        addResources(this)

        PreferenceScreen.PLAYER.addPreferences(
            SwitchPreference("revanced_hide_cast_button"),
        )

        val buttonClass = context.classes.find {
            it.methods.any {
                it.name == "MediaRouteButton"
            }
        } ?: throw PatchException("MediaRouteButton class not found.")

        context.proxy(buttonClass).mutableClass.methods.find { it.name == "setVisibility" }?.addInstructions(
            0,
            """
                    invoke-static {p1}, Lapp/revanced/integrations/youtube/patches/HideCastButtonPatch;->getCastButtonOverrideV2(I)I
                    move-result p1
                """,
        )
    }
}
