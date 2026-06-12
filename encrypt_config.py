import json
import base64
import os
import hashlib
import sys

# use hashlib for key derivation to be more reliable across environments
def get_key(password, salt):
    # dkLen=32 for 256-bit key, iterations=1000, hash=sha1
    # matches Android's "PBKDF2WithHmacSHA1"
    return hashlib.pbkdf2_hmac('sha1', password.encode('utf-8'), salt, 1000, 32)

def encrypt_lts(plain_text, password):
    try:
        from Crypto.Cipher import AES
        from Crypto.Util.Padding import pad

        # EXACTLY 16 BYTES SALT
        salt = b'lts_demo_salt_16'
        key = get_key(password, salt)

        # AES CBC requires 16 bytes IV
        iv = os.urandom(16)
        cipher = AES.new(key, AES.MODE_CBC, iv)
        ct_bytes = cipher.encrypt(pad(plain_text.encode('utf-8'), AES.block_size))

        # Pack: salt (16) + iv (16) + ciphertext
        combined = salt + iv + ct_bytes
        return base64.b64encode(combined).decode('utf-8')
    except ImportError:
        print("\n[错误] 缺少 'pycryptodome' 依赖。请运行: python3 -m pip install pycryptodome")
        return None
    except Exception as e:
        print(f"\n[错误] 加密过程中出错: {e}")
        return None

if __name__ == "__main__":
    base_dir = os.path.dirname(os.path.abspath(__file__))
    input_file = os.path.join(base_dir, 'app/src/main/assets/configs.json')
    output_file = os.path.join(base_dir, 'app/src/main/assets/configs_encrypted.json')

    if not os.path.exists(input_file):
        print(f"错误: 找不到输入文件 {input_file}")
        sys.exit(1)

    print("--- 华为云 LTS 配置加密工具 (最终修复版) ---")
    password = input("请输入加密密码 (例如 123456): ")

    if not password:
        print("错误: 密码不能为空")
        sys.exit(1)

    try:
        with open(input_file, 'r', encoding='utf-8') as f:
            data = f.read()

        encrypted_data = encrypt_lts(data, password)

        if encrypted_data:
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(encrypted_data)
            print(f"\n[成功] 加密完成！")
            print(f"输出文件: {output_file}")
            print(f"请重新运行 App，并在“加载加密配置”时输入相同密码。")
    except Exception as e:
        print(f"运行出错: {e}")
