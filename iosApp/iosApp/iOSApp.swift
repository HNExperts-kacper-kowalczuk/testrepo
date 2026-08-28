import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinKt.doInitKoin()
        donateScanActivity()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onContinueUserActivity(Self.scanActivityType) { _ in
                    IosShortcutsKt.requestScanBarcodeFromShortcut()
                }
        }
    }

    private static let scanActivityType = "com.hnexperts.cosmetics.scanner.scan"

    private func donateScanActivity() {
        let activity = NSUserActivity(activityType: Self.scanActivityType)
        activity.title = "Scan barcode"
        activity.isEligibleForSearch = true
        activity.isEligibleForPrediction = true
        activity.becomeCurrent()
    }
}
