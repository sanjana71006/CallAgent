"""
CallMate AI - Terminal Live Call Simulator
Interactive CLI to test AI screening, real-time responses, classification, and summary generation.
"""

import asyncio
import httpx
import time

BASE_URL = "http://localhost:8000"

async def run_simulation():
    print("\n" + "="*65)
    print("      [CALLMATE AI] LIVE PHONE SCREENING SIMULATOR")
    print("             'Let AI answer. You decide.'")
    print("="*65 + "\n")

    # 1. Health check
    async with httpx.AsyncClient(timeout=10.0) as client:
        try:
            resp = await client.get(f"{BASE_URL}/api/v1/health")
            health_data = resp.json()
            print(f"[STATUS] Backend: {health_data.get('status').upper()} | Version: {health_data.get('version')}")
            print(f"[STATUS] AI Engine: {health_data.get('ai', {}).get('provider')}\n")
        except Exception as e:
            print(f"[ERROR] Could not connect to backend at {BASE_URL}: {e}")
            return

    # 2. Simulate Incoming Call
    caller_name = "Sarah Jenkins (Recruiter)"
    caller_phone = "+1 (555) 382-9012"
    call_id = f"sim-{int(time.time())}"

    print(f"[*] INCOMING CALL from: {caller_name} ({caller_phone})")
    print("[*] CallMate AI is answering on your behalf...\n")
    time.sleep(1)

    conversation = []
    
    # Initial Greeting
    greeting = "Hello! I am CallMate AI, screening this call on behalf of the user. How may I assist you today?"
    conversation.append({"speaker": "ai", "text": greeting})
    print(f">> [AI ASSISTANT]: \"{greeting}\"\n")

    # Interactive simulation turns
    sample_inputs = [
        "Hello! I am calling from Google regarding your senior software engineer job application to schedule an interview.",
        "Could we schedule a 45-minute technical discussion for this Thursday at 10:30 AM?",
        "Great, please ask the candidate to confirm their availability via email. Thank you!"
    ]

    for turn_idx, default_msg in enumerate(sample_inputs, 1):
        time.sleep(1)
        print(f">> [CALLER]: \"{default_msg}\"")
        conversation.append({"speaker": "caller", "text": default_msg})

        # Ask backend AI
        print("... [AI THINKING] Analyzing context & generating polite screening response...")
        async with httpx.AsyncClient(timeout=15.0) as client:
            resp = await client.post(
                f"{BASE_URL}/api/v1/ai/chat",
                json={
                    "call_id": call_id,
                    "conversation": conversation,
                    "caller_name": caller_name,
                    "caller_phone": caller_phone,
                    "assistant_name": "CallMate AI"
                }
            )
            chat_data = resp.json()
            ai_reply = chat_data.get("response")
            conversation.append({"speaker": "ai", "text": ai_reply})
            print(f">> [AI ASSISTANT]: \"{ai_reply}\"\n")

            if chat_data.get("is_call_complete"):
                break

    print("[*] CALL CONCLUDED. Generating post-call AI classification & structured summary...\n")
    time.sleep(1)

    # 3. Post-call classification & summary
    async with httpx.AsyncClient(timeout=15.0) as client:
        classify_resp = await client.post(
            f"{BASE_URL}/api/v1/ai/classify",
            json={"call_id": call_id, "conversation": conversation}
        )
        classify_data = classify_resp.json()

        summary_resp = await client.post(
            f"{BASE_URL}/api/v1/ai/summarize",
            json={"call_id": call_id, "conversation": conversation, "caller_name": caller_name}
        )
        summary_data = summary_resp.json()

    print("="*65)
    print("                    CALLMATE AI SUMMARY")
    print("="*65)
    print(f"- Caller:             {summary_data.get('caller')}")
    print(f"- Category:           {classify_data.get('category')} (Confidence: {classify_data.get('confidence')})")
    print(f"- Priority / Level:   {classify_data.get('importance')} IMPORTANCE")
    print(f"- Purpose:            {summary_data.get('purpose')}")
    print(f"- Key Details:        {summary_data.get('important_details')}")
    print(f"- Recommended Action: {summary_data.get('recommended_action')}")
    print(f"- Is Spam:            {'YES' if classify_data.get('is_spam') else 'NO'}")
    print(f"- Executive Recap:    {summary_data.get('executive_summary')}")
    print("="*65)
    print("[*] Saved to local database history. Available in Call Details.\n")

if __name__ == "__main__":
    asyncio.run(run_simulation())
