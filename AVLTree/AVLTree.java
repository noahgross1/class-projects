/**
 * AVLTree class.
 * @param <T> data type
 */
public class AVLTree<T extends Comparable<? super T>> extends BinarySearchTree<T> {
	

	/**
	 * Default constructor.
	 */
	public AVLTree() {
		super();
	}
	
	/**
	 * Constructor. 
	 * @param rootEntry item to store in root
	 */
	public AVLTree(T rootEntry) {
		super(rootEntry);
	}
	
	/**
	 * Add new entry to AVL tree.
	 * @param newEntry new entry in AVL tree
	 * @return newEntry new entry in AVL tree
	 */
	public T add(T newEntry) {
		if(isEmpty()) {
			setRootNode(new AVLNode<>(newEntry));
		} else {
			//Recursively insert node in subtree of tree.
			BinaryNode<T> root = this.root;
			this.root = addEntry(root, newEntry);
		}
	    return newEntry;
	}
	
	/**
	 * Adds new entry to the nonempty subtree rooted at rootNode.
	 * @param rootNode reference to root of tree
	 * @param newEntry entry to be added in tree
	 * @return newEntry entry added in AVL tree
	 */
	private BinaryNode<T> addEntry(BinaryNode<T> rootNode, T newEntry) {
	      int comparison = newEntry.compareTo(rootNode.getData());
	      
	      //Data is same.
	      if (comparison == 0) {
	         rootNode.setData(newEntry);
	      }
	      //New entry is less than root data.
	      else if (comparison < 0) {
	         if (rootNode.hasLeftChild())
	            rootNode.setLeftChild(addEntry(rootNode.getLeftChild(), newEntry));
	         else
	            rootNode.setLeftChild(new AVLNode<>(newEntry));
	      }
	      else {
	         //New entry is greater than root data.
	         if (rootNode.hasRightChild()) {
	            rootNode.setRightChild(addEntry(rootNode.getRightChild(), newEntry));
	         } else {
	            rootNode.setRightChild(new AVLNode<>(newEntry));
	         }
	      } // end
	      
	      //If there is a height imbalance, do single or double rotation.
	      rootNode = rebalance(rootNode);
	      return rootNode;
	}

	/**
	 * Remove element from AVL tree.
	 * @param entry element in AVL tree
	 * @return element to be removed
	 */
	public T remove(T entry) {
		if (this.root == null) {
			throw new EmptyTreeException();
		}
	    ReturnObject oldEntry = new ReturnObject(null);
	    BinaryNode<T> newRoot = removeEntry(getRootNode(), entry, oldEntry);
	    setRootNode(newRoot);
	      
	    return oldEntry.get();
	}
	
   /**
    * Removes an entry from the tree rooted at a given node.
    * @param rootNode a reference to the root of a tree
    * @param entry the object to be removed
    * @param oldEntry an object whose data field is null
    * @return root node of the resulting tree; if entry matches 
    *         an entry in the tree, oldEntry's data field is the entry
    *         that was removed from the tree; otherwise it is null
    */
   private BinaryNode<T> removeEntry(BinaryNode<T> rootNode, T entry, ReturnObject oldEntry) {
      if (rootNode != null) {
         T rootData = rootNode.getData();
         int comparison = entry.compareTo(rootData);
         
         if (comparison == 0) {      // anEntry == root entry
            oldEntry.set(rootData);
            rootNode = removeFromRoot(rootNode);
         }
         else if (comparison < 0) {   // anEntry < root entry
            BinaryNode<T> leftChild = rootNode.getLeftChild();
            BinaryNode<T> subtreeRoot = removeEntry(leftChild, entry, oldEntry);
            rootNode.setLeftChild(subtreeRoot);
         }
         else {                      // anEntry > root entry
            BinaryNode<T> rightChild = rootNode.getRightChild();
            // A different way of coding than for left child:
            rootNode.setRightChild(removeEntry(rightChild, entry, oldEntry));
         } // end if
         rootNode = rebalance(rootNode);
      } // end if
      
      return rootNode;
   } // end removeEntry
   
   
  /**
   * Removes the entry in a given root node of a subtree.
   * @param rootNode is the root node of the subtree
   * @return the root node of the revised subtree
   */
  private BinaryNode<T> removeFromRoot(BinaryNode<T> rootNode)
  {
     // Case 1: rootNode has two children
     if (rootNode.hasLeftChild() && rootNode.hasRightChild()) {
        // Find node with largest entry in left subtree
        BinaryNode<T> leftSubtreeRoot = rootNode.getLeftChild();
        BinaryNode<T> largestNode = findLargest(leftSubtreeRoot);
        
        // Replace entry in root
        rootNode.setData(largestNode.getData());
        
        // Remove node with largest entry in left subtree
        rootNode.setLeftChild(removeLargest(leftSubtreeRoot));
     } // end if
     
     // Case 2: rootNode has at most one child
     else if (rootNode.hasRightChild()) {
        rootNode = rootNode.getRightChild();
     } else {
        rootNode = rootNode.getLeftChild();
     }
     
     // Assertion: If rootNode was a leaf, it is now null
     
     return rootNode;
  } // end removeFromRoot
   
  
  /**
   * Finds the node containing the largest entry in a given tree.
   * @param rootNode is the root node of the tree
   * @return the node containing the largest entry in the tree
   */
   private BinaryNode<T> findLargest(BinaryNode<T> rootNode)
   {
      if (rootNode.hasRightChild()) {
         rootNode = findLargest(rootNode.getRightChild());
      }
      
      return rootNode;
   } // end findLargest
   
   /**
    * Removes the node containing the largest entry in a given tree.
    * @param rootNode is the root node of the tree
    * @return the root node of the revised tree
    */
   private BinaryNode<T> removeLargest(BinaryNode<T> rootNode)
   {
      if (rootNode.hasRightChild()) {
         BinaryNode<T> rightChild = rootNode.getRightChild();
         rightChild = removeLargest(rightChild);
         rootNode.setRightChild(rightChild);
      } else {
         rootNode = rootNode.getLeftChild();
      }
      
      return rootNode;
   }
   // end removeLargest

	
	/**
	 * Single Right Rotation.
	 * @param node in tree
	 * @return rotated node
	 */
	private BinaryNode<T> singleRightRotation(BinaryNode<T> node) {
		BinaryNode<T> rotatedNode = node.getLeftChild();
		node.setLeftChild(rotatedNode.getRightChild());
		rotatedNode.setRightChild(node);
		return rotatedNode;	
	}
	
	/**
	 * Single Left Rotation.
	 * @param node in tree
	 * @return rotated node
	 */
	private BinaryNode<T> singleLeftRotation(BinaryNode<T> node) {
		BinaryNode<T> rotatedNode = node.getRightChild();
		node.setRightChild(rotatedNode.getLeftChild());
		rotatedNode.setLeftChild(node);
		return rotatedNode;
	}
	
	/**
	 * Right left double rotation.
	 * @param node node in tree
	 * @return node
	 */
	private BinaryNode<T> rightLeftDoubleRotation(BinaryNode<T> node) {
		BinaryNode<T> rotatedNode = node.getRightChild();
		BinaryNode<T> rightRotatedNode = singleRightRotation(rotatedNode);
		node.setRightChild(rightRotatedNode);
		node = singleLeftRotation(node);
		return node;
	}
	
	/**
	 * Left right double rotation.
	 * @param node node in tree
	 * @return node 
	 */
	private BinaryNode<T> leftRightDoubleRotation(BinaryNode<T> node) {
		BinaryNode<T> rotatedNode =  node.getLeftChild();
		BinaryNode<T> leftRotatedNode = singleLeftRotation(rotatedNode);
		node.setLeftChild(leftRotatedNode);
		node = singleRightRotation(node);
		return node;
	}
	
	/**
	 * Balance height of AVL tree to height difference of 0 or 1.
	 * @param node node in tree
	 * @return node 
	 */
	private BinaryNode<T> rebalance(BinaryNode<T> node) {
		int hDiff = getBalanceFactor(node);
		
		if(hDiff > 1) {
			//Check if left subtree's height is greater than 0.
			if(getBalanceFactor(node.getLeftChild()) >= 0) {
				//Addition was in left child of nodes left subtree.
				//Rotate right to balance tree.
				node = singleRightRotation(node);
			} else {
				//Addition was in right child of nodes left subtree.
				//Rotate left then right to balance tree.
				node = leftRightDoubleRotation(node);
			}
		}
		else if(hDiff < -1) {
			if(getBalanceFactor(node.getRightChild()) < 0) {
				//Addition was in right child of nodes right subtree.
				node = singleLeftRotation(node);
			} else {
				//Addition was in left child of nodes right subtree.
				node = rightLeftDoubleRotation(node);
			}
		}			
		return node;		
	}
	
	/**
	 * Gets the height of the tree. 
	 * @param node node in tree
	 * @return height of tree
	 */
	private int getHeight(BinaryNode<T> node) {
      int height = -1;

      if (node != null && node.getData() != null) {
         height = 1 + Math.max(getHeight(node.getLeftChild()),
                               getHeight(node.getRightChild()));
      }
                             
      return height;
	} // end getHeight 
	
	/**
	 * Get difference in height between left and right subtrees.
	 * @return height difference 
	 */
	private int getBalanceFactor(BinaryNode<T> node) {
		if(node != null) {
			int leftHeight = -1;
			if(node.hasLeftChild()) {
				leftHeight = getHeight(node.getLeftChild());
			}
			int rightHeight = -1;
			if(node.hasRightChild()) {
				rightHeight = getHeight(node.getRightChild());
			}
			
			return leftHeight - rightHeight;
		}
		
		return 0;
	}
	
	/**
	 * Wrapper for entry. 
	 */
	private class ReturnObject
	{
		/**
		 * Entry in a tree.
		 */
		private T item;
		
		/**
		 * Constructor.
		 * @param entry entry in tree
		 */
		private ReturnObject(T entry)
		{
			item = entry;
		} // end constructor
		
		/**
		 * Retrieve item.
		 * @return item
		 */
		public T get()
		{
			return item;
		} // end get

		/**
		 * Set item.
		 * @param entry entry in wrapper
		 */
		public void set(T entry)
		{
			item = entry;
		} // end set
	} // end ReturnObject

}
