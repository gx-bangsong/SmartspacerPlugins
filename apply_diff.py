import sys

def apply_diff(file_path, diff_path):
    with open(file_path, 'r') as f:
        content = f.read()

    with open(diff_path, 'r') as f:
        diff = f.read()

    parts = diff.split('<<<<<<< SEARCH')
    for part in parts[1:]:
        search_replace = part.split('=======')
        search = search_replace[0].strip('\n')
        replace = search_replace[1].split('>>>>>>> REPLACE')[0].strip('\n')

        if search in content:
            content = content.replace(search, replace)
        else:
            print(f"Warning: Search block not found in {file_path}")
            # Try with less strict matching (strip whitespace from each line)
            search_lines = search.split('\n')
            content_lines = content.split('\n')

            found = False
            for i in range(len(content_lines) - len(search_lines) + 1):
                match = True
                for j in range(len(search_lines)):
                    if content_lines[i+j].strip() != search_lines[j].strip():
                        match = False
                        break
                if match:
                    # Found it!
                    new_content_lines = content_lines[:i] + replace.split('\n') + content_lines[i+len(search_lines):]
                    content = '\n'.join(new_content_lines)
                    found = True
                    break
            if not found:
                print(f"Error: Could not find search block even with loose matching in {file_path}")
                sys.exit(1)

    with open(file_path, 'w') as f:
        f.write(content)

if __name__ == "__main__":
    apply_diff(sys.argv[1], sys.argv[2])
