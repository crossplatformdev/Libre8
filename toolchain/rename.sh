#!/usr/bin/env bash
# rename.sh - Rename files matching ff_00000_*.jpg to ff_00000_00000.jpg, ff_00000_00001.jpg, ...
# Usage: ./rename.sh [directory]
set -euo pipefail

shopt -s nullglob

dir="${1:-.}"
cd "$dir" || { echo "Cannot enter directory: $dir" >&2; exit 1; }

files=(ff_00000_*.jpg)
if [[ ${#files[@]} -eq 0 ]]; then
    echo "No files matching ff_00000_*.jpg in $dir" >&2
    exit 0
fi

# Build a sortable list: "numeric_suffix<TAB>filename"
tmplist="$(mktemp)"
trap 'rm -f "$tmplist"' EXIT

for f in "${files[@]}"; do
    suffix="${f#ff_00000_}"
    suffix="${suffix%.jpg}"
    if [[ $suffix =~ ^[0-9]+$ ]]; then
        # Interpret as base-10 even if padded with zeros
        num=$((10#$suffix))
    else
        # Non-numeric suffixes go last (use large number)
        num=$((10**9))
    fi
    printf '%d\t%s\n' "$num" "$f" >> "$tmplist"
done

# Sort by numeric suffix (ascending)
sort -n "$tmplist" -o "$tmplist"

# Phase 1: move originals to temporary unique names to avoid collisions
tmp_prefix=".rename_tmp_$$"
tmpfiles=()
index=0
while IFS=$'\t' read -r _fname fname; do
    # tmp name includes index to preserve order and avoid collisions
    tmpname="${tmp_prefix}_$(printf '%05d' "$index")"
    mv -- "$fname" "$tmpname"
    tmpfiles+=("$tmpname")
    index=$((index + 1))
done < "$tmplist"

# Phase 2: move temps to final names ff_00000_00000.jpg, ff_00000_00001.jpg, ...
final_index=0
for t in "${tmpfiles[@]}"; do
    dest=$(printf 'ff_00000_%05d.jpg' "$final_index")
    if [[ -e $dest ]]; then
        echo "Error: destination already exists: $dest" >&2
        # Attempt to restore moved files (best-effort)
        for restored_index in "${!tmpfiles[@]}"; do
            [[ -e "${tmpfiles[$restored_index]}" ]] && mv -- "${tmpfiles[$restored_index]}" "ff_00000_$(printf '%05d' "$restored_index").jpg" || true
        done
        exit 1
    fi
    mv -- "$t" "$dest"
    final_index=$((final_index + 1))
done

echo "Renamed ${final_index} files to ff_00000_00000.jpg .. ff_00000_$(printf '%05d' "$((final_index-1))").jpg"