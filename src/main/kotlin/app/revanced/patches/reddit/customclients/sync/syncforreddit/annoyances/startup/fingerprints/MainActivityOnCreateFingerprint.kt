package app.revanced.patches.reddit.customclients.sync.syncforreddit.annoyances.startup.fingerprints

import app.revanced.patcher.fingerprint.methodFingerprint

internal val mainActivityOnCreateFingerprint = methodFingerprint {
    custom { method, classDef ->
        classDef.endsWith("MainActivity;") && method.name == "onCreate"
    }
}