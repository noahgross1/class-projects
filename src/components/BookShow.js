import { useState } from 'react';
import BookEdit from './BookEdit';

function BookShow({ book, onDelete, onEdit }) {
    const [showEdit, setShowEdit] = useState(false);

    /**
     * Delete book.
     */
    const handleDeleteClick = () => {
        onDelete(book.id);
    };

    const handleEditClick  = () => {
        setShowEdit(!showEdit);
    };

    /**
     * Close form after updating title
     * @param {*} id id of book to edit
     * @param {*} newTitle newTitle of book 
     */
    const handleSubmit = (id, newTitle) => {
        onEdit(id, newTitle);
        setShowEdit(false);
    };

    let content = <h3>{book.title}</h3>;
    if (showEdit) {
        content = <BookEdit onSubmit={handleSubmit} book={book}/>;
    }

    return (
        <div className="book-show">
            <img 
                alt="books"
                src={`https://picsum.photos/seed/${book.id}/300/200`}
            />
            <div>{content}</div>
            <div className="actions">
                <button className='edit' onClick={handleEditClick}>
                    Edit
                </button>
                <button className="delete" onClick={handleDeleteClick}>
                    Delete
                </button>
            </div>
        </div>
    );
}

export default BookShow;