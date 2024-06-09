package app.revanced.patches.youtube.layout.buttons.navigation

import app.revanced.patcher.extensions.InstructionExtensions.addInstruction
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference
import app.revanced.patches.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import app.revanced.patches.shared.misc.settings.preference.SwitchPreference
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.ANDROID_AUTOMOTIVE_STRING
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.addCreateButtonViewFingerprint
import app.revanced.patches.youtube.layout.buttons.navigation.fingerprints.createPivotBarFingerprint
import app.revanced.patches.youtube.misc.integrations.integrationsPatch
import app.revanced.patches.youtube.misc.navigation.hookNavigationButtonCreated
import app.revanced.patches.youtube.misc.navigation.navigationBarHookPatch
import app.revanced.patches.youtube.misc.settings.PreferenceScreen
import app.revanced.patches.youtube.misc.settings.settingsPatch
import app.revanced.util.getReference
import app.revanced.util.indexOfFirstInstruction
import com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

internal const val INTEGRATIONS_CLASS_DESCRIPTOR =
    "Lapp/revanced/integrations/youtube/patches/NavigationButtonsPatch;"

@Suppress("unused")
val navigationButtonsPatch = bytecodePatch(
    name = "Navigation buttons",
    description = "Adds options to hide and change navigation buttons (such as the Shorts button).",
) {
    dependsOn(
        integrationsPatch,
        settingsPatch,
        addResourcesPatch,
        navigationBarHookPatch,
    )

    compatibleWith(
        "com.google.android.youtube"(
            "18.32.39",
            "18.37.36",
            "18.38.44",
            "18.43.45",
            "18.44.41",
            "18.45.43",
            "18.48.39",
            "18.49.37",
            "19.01.34",
            "19.02.39",
            "19.03.36",
            "19.04.38",
            "19.05.36",
            "19.06.39",
            "19.07.40",
            "19.08.36",
            "19.09.38",
            "19.10.39",
            "19.11.43",
        ),
    )

    val addCreateButtonViewResult by addCreateButtonViewFingerprint
    val createPivotBarResult by createPivotBarFingerprint

    execute {
        addResources("youtube", "layout.buttons.navigation.NavigationButtonsPatch")

        PreferenceScreen.GENERAL_LAYOUT.addPreferences(
            PreferenceScreenPreference(
                key = "revanced_navigation_buttons_screen",
                sorting = Sorting.UNSORTED,
                preferences = setOf(
                    SwitchPreference("revanced_hide_home_button"),
                    SwitchPreference("revanced_hide_shorts_button"),
                    SwitchPreference("revanced_hide_create_button"),
                    SwitchPreference("revanced_hide_subscriptions_button"),
                    SwitchPreference("revanced_switch_create_with_notifications_button"),
                    SwitchPreference("revanced_hide_navigation_button_labels"),
                ),
            ),
        )

        // Switch create with notifications button.
        addCreateButtonViewResult.mutableMethod.apply {
            val stringIndex = addCreateButtonViewResult.scanResult.stringsScanResult!!.matches.find { match ->
                match.string == ANDROID_AUTOMOTIVE_STRING
            }!!.index

            val conditionalCheckIndex = stringIndex - 1
            val conditionRegister =
                getInstruction<OneRegisterInstruction>(conditionalCheckIndex).registerA

            addInstructions(
                conditionalCheckIndex,
                """
                    invoke-static { }, $INTEGRATIONS_CLASS_DESCRIPTOR->switchCreateWithNotificationButton()Z
                    move-result v$conditionRegister
                """,
            )
        }

        // Hide navigation button labels.
        createPivotBarResult.mutableMethod?.apply {
            val setTextIndex = indexOfFirstInstruction {
                getReference<MethodReference>()?.name == "setText"
            }

            val targetRegister = getInstruction<FiveRegisterInstruction>(setTextIndex).registerC

            addInstruction(
                setTextIndex,
                "invoke-static { v$targetRegister }, " +
                    "$INTEGRATIONS_CLASS_DESCRIPTOR->hideNavigationButtonLabels(Landroid/widget/TextView;)V",
            )
        }

        // Hook navigation button created, in order to hide them.
        hookNavigationButtonCreated(INTEGRATIONS_CLASS_DESCRIPTOR)
    }
}
