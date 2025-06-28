import os
import re
import subprocess
import sys


def get_cuda_version() -> str | None:
    """Detect CUDA version from nvidia-smi output."""
    try:
        output = subprocess.check_output(["nvidia-smi"], encoding="utf-8")
    except (OSError, subprocess.CalledProcessError):
        print("❌ Could not detect CUDA version. Is NVIDIA driver installed?")
        return None

    match = re.search(r"CUDA Version: (\d+\.\d+)", output)
    if match:
        return match.group(1)
    return None


def resolve_cu_tag(cuda_version: str | None) -> str:
    """Return the correct PyTorch cuXXX tag based on detected CUDA version."""
    if not cuda_version:
        return "cpu"

    try:
        major, minor = map(int, cuda_version.split("."))
        version_float = major + minor / 10
    except ValueError:
        print(f"⚠️ Unable to parse CUDA version: {cuda_version}")
        return "cpu"

    if version_float >= 12.1:
        return "cu121"
    elif version_float >= 11.8:
        return "cu118"
    elif version_float >= 11.7:
        return "cu117"
    else:
        return "cpu"


def install_packages(cuda_version: str | None) -> None:
    """Install torch and transformers with the proper CUDA tag."""
    base_cmd = [sys.executable, "-m", "pip", "install", "--no-cache-dir"]

    cu_tag = resolve_cu_tag(cuda_version)

    if cu_tag == "cpu":
        print(f"❌ No compatible GPU build for CUDA {cuda_version}. Aborting install.")
        sys.exit(1)

    index_url = f"https://download.pytorch.org/whl/{cu_tag}"
    print(f"🚀 Installing PyTorch for CUDA {cuda_version} (build tag: {cu_tag})")

    try:
        subprocess.check_call(base_cmd + [
            "--extra-index-url", index_url,
            "torch", "torchvision", "torchaudio", "transformers"
        ])
        print("✅ Installation successful.")
    except subprocess.CalledProcessError:
        print("❌ GPU build install failed. Check PyTorch compatibility or your CUDA driver.")
        sys.exit(1)


def main() -> None:
    print("🔎 Detecting system CUDA version...")
    cuda_version = get_cuda_version()
    print(f"🧠 Detected CUDA version: {cuda_version}")

    print("🧼 Uninstalling existing PyTorch packages (if any)...")
    subprocess.call([
        sys.executable, "-m", "pip", "uninstall", "-y",
        "torch", "torchvision", "torchaudio"
    ])

    install_packages(cuda_version)


if __name__ == "__main__":
    main()
