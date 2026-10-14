"use client";

import { useRef, useState } from "react";
import { assetUrl, uploadImage } from "@/lib/api";

interface Props {
  images: string[];
  onChange: (urls: string[]) => void;
}

export default function ImageUploader({ images, onChange }: Props) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");

  const handlePick = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    e.target.value = "";
    if (!file) return;
    if (images.length >= 3) {
      setError("最多 3 张图片");
      return;
    }
    setError("");
    setBusy(true);
    try {
      const { url } = await uploadImage(file);
      onChange([...images, url]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "上传失败");
    } finally {
      setBusy(false);
    }
  };

  const remove = (i: number) => onChange(images.filter((_, idx) => idx !== i));

  return (
    <div>
      <div style={{ display: "flex", gap: 6, flexWrap: "wrap" }}>
        {images.map((url, i) => (
          <div key={url} style={{ position: "relative" }}>
            <img
              src={assetUrl(url)}
              alt={`配图 ${i + 1}`}
              style={{
                width: 52,
                height: 52,
                objectFit: "cover",
                borderRadius: 4,
                border: "1px solid var(--line)",
                display: "block",
              }}
            />
            <span
              onClick={() => remove(i)}
              style={{
                position: "absolute",
                top: -6,
                right: -6,
                width: 16,
                height: 16,
                borderRadius: "50%",
                background: "var(--blue-deep)",
                color: "#fff",
                fontSize: 10,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "pointer",
                lineHeight: 1,
              }}
            >
              ×
            </span>
          </div>
        ))}
        {images.length < 3 && (
          <button
            type="button"
            onClick={() => inputRef.current?.click()}
            disabled={busy}
            style={{
              width: 52,
              height: 52,
              borderRadius: 4,
              border: "1.5px solid var(--line-strong)",
              background: "var(--card)",
              fontSize: 20,
              color: "var(--blue-deep)",
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
            }}
          >
            ＋
          </button>
        )}
        <input
          ref={inputRef}
          type="file"
          accept="image/jpeg,image/png,image/webp"
          hidden
          onChange={handlePick}
        />
      </div>
      {busy && <div className="hint">上传中…</div>}
      {error && <div className="err">{error}</div>}
    </div>
  );
}
