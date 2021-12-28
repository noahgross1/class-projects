
/**
 * AVL tree node class.
 * @param <T> data type
 */
public class AVLNode<T> extends BinaryNode<T> {
	
	/**
	 * Height of subtree rooted at a specific node.
	 */
	private int height;
	
	
	/**
	 * Default constructor. 
	 */
	public AVLNode() {
		super();
		this.height = -1;
	}
	
	/**
	 * Default constructor. 
	 * @param data data in node
	 */
	public AVLNode(T data) {
		super(data);
		this.height = 0; 
	}
	
	/**
	 * Constructor.
	 * @param data data in node
	 * @param leftNode left node of parent
	 * @param rightNode right node of parent
	 */
	public AVLNode(T data, AVLNode<T> leftNode, AVLNode<T> rightNode) {
		setData(data);
		setLeftChild(leftNode);
		setRightChild(rightNode);
		this.height = getHeight(this);
	}
	
	/**
	 * Modifies left node of parent.
	 * @param leftNode left node of parent
	 */
	public void setLeftChild(AVLNode<T> leftNode) {
		 super.setLeftChild(leftNode);
		 getHeight(this);
	}
	
	/**
	 * Modify right node of parent.
	 * @param rightNode right node of parent
	 */
	public void setRightChild(AVLNode<T> rightNode) {
		super.setRightChild(rightNode);
		this.height = getHeight(this);
	}
	
	/**
	 * Retrieves height of AVL tree.
	 */
	public int getHeight() {
		return getHeight(this);
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
}
