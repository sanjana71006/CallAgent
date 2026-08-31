# Real Telephony Integration & Android OS Limitations

## 1. Android Telephony Architecture Landscape

Implementing an automated AI call screener on modern Android (API 29+) involves strict OS security policies, permission constraints, and Google Play Store distribution policies.

---

## 2. Supported Mechanisms vs Restricted Behaviors

| Feature / Goal | Android Mechanism | Feasibility & Requirements | Limitations & Policies |
|---|---|---|---|
| **Call Screening (Metadata)** | `android.telecom.CallScreeningService` | Fully Supported (API 24+) | Can allow/disallow/silence incoming calls and query caller ID. **Cannot directly inject synthetic two-way audio** into an active carrier phone line without being the default dialer. |
| **In-Call Answering & Audio** | `android.telecom.InCallService` | Supported ONLY when set as the **Default Phone / Dialer App** | Requires user prompt to make app default phone handler (`TelecomManager.ACTION_CHANGE_DEFAULT_DIALER`). Google Play restricts dialer role permissions. |
| **Two-Way Call Audio Stream (VoIP / Cellular)** | `AudioRecord` / `AudioPlaybackCapture` / `Telecom ConnectionService` | VoIP: Fully supported. Cellular: Highly restricted on non-rooted devices due to carrier and hardware hardware-isolation (voice downlink/uplink audio routing). | Android OS isolates baseband cellular audio stream for privacy and wiretapping regulations. Direct programmatic injection of synthesized TTS audio into cellular uplink requires specialized telecom SIP/PSTN trunking (e.g. Twilio / Asterisk / Telnyx) in production. |

---

## 3. Recommended Production Telephony Roadmap

1. **MVP / Prototyping Stage (Current)**:
   - Use `SimulatorCallProvider` for complete offline validation and speech loop testing.
   - Use `CallScreeningService` on-device to inspect incoming numbers and notify user.
2. **Production VoIP / Cloud PSTN Bridge**:
   - Route incoming carrier calls via operator call forwarding (Conditional Call Forwarding / Busy / Unanswered) to a SIP/WebRTC endpoint managed by CallMate AI backend.
   - Backend performs real-time STT -> Ollama/LLM -> TTS -> WebRTC audio streaming to the caller.
   - Android app receives live transcript over WebSocket and allows seamless user takeover via WebRTC.
3. **Default Dialer Mode (On-Device)**:
   - Implement full `InCallService` UI for managing native telephony calls when user explicitly chooses CallMate AI as default phone app.
