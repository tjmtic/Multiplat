import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        // Default: stub engine, so the OTA demo runs without an on-device model.
        MainViewControllerKt.MainViewController()

        // To run a real on-device model (iOS 26+ Foundation Models shown), use the overload:
        //
        // MainViewControllerKt.MainViewController { prompt, onResult, onError in
        //     Task {
        //         do {
        //             let session = LanguageModelSession()
        //             let reply = try await session.respond(to: prompt)
        //             onResult(reply.content)
        //         } catch { onError(KotlinThrowable(message: "\(error)")) }
        //     }
        // }
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
    }
}



