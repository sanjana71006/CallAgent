import os
from doc_builder.core import init_doc
from doc_builder.sec1_to_9 import build_sec1_to_9
from doc_builder.sec10_to_18 import build_sec10_to_18
from doc_builder.sec19_to_27 import build_sec19_to_27
from doc_builder.sec28_to_36 import build_sec28_to_36

def main():
    print("[1/5] Initializing document & styles...")
    doc = init_doc()
    
    print("[2/5] Writing Sections 1 to 9...")
    build_sec1_to_9(doc)
    
    print("[3/5] Writing Sections 10 to 18...")
    build_sec10_to_18(doc)
    
    print("[4/5] Writing Sections 19 to 27...")
    build_sec19_to_27(doc)
    
    print("[5/5] Writing Sections 28 to 36...")
    build_sec28_to_36(doc)
    
    output_filename = "CallMate_AI_Complete_Project_Documentation.docx"
    doc.save(output_filename)
    file_size_kb = os.path.getsize(output_filename) / 1024
    print(f"SUCCESS: Saved {output_filename} ({file_size_kb:.2f} KB)")

if __name__ == "__main__":
    main()
