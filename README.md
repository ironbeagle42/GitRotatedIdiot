# GitRotatedIdiot
# Git.init() now works, creates correct files
# Git.hashString() now works, turns a string into a SHA-1 hash
# Git.blob() now works, turns a string into a blob file with title of that string's hash and contents of that string
# Git.getTextOfFile() now works, reads a file and returns its contents as a string
# Git.indexFile() now works, adds a file's hash and relative path to the index or updates its existing entry
# Git.createTree() now works, creates a tree file for one directory from a working list and returns its hash
# Git.treeIndex() now works, creates trees from the files in the index and returns the root tree's hash
# Git.commit() now works, saves a commit with its root tree and previous commit, updates HEAD, and returns the commit's hash
# GitWrapper calls the existing Git methods using the method names from normal git
# Git.rebuildIndex() now works, reads a commits trees and replaces the index with that snapshots file hashes and paths
# Git.checkout() now works, restores a commits files, removes files that are no longer in the snapshot and updates the index and HEAD