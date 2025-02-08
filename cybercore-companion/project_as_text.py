import os

def save_files_as_text(src_dir, dest_dir, exclude_dirs):
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)
    
    for root, dirs, files in os.walk(src_dir):
        # Exclude specified directories
        dirs[:] = [d for d in dirs if d not in exclude_dirs]
        
        for file in files:
            src_file_path = os.path.join(root, file)
            dest_file_path = os.path.join(dest_dir, file + '.txt')
            
            try:
                # Try reading the file as text with UTF-8 encoding
                with open(src_file_path, 'r', encoding='utf-8') as src_file:
                    content = src_file.read()
                
                # Write the content to the destination file
                with open(dest_file_path, 'w', encoding='utf-8') as dest_file:
                    dest_file.write(content)
            except UnicodeDecodeError:
                # Skip binary files or files with incompatible encodings
                print(f"Skipping binary or incompatible file: {src_file_path}")
            except Exception as e:
                # Handle other potential errors
                print(f"Error processing file {src_file_path}: {e}")

if __name__ == "__main__":
    src_directory = os.path.dirname(os.path.abspath(__file__))
    dest_directory = os.path.join(src_directory, "project as text")
    exclude_directories = ["target"]
    
    save_files_as_text(src_directory, dest_directory, exclude_directories)