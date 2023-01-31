import BookShow from './BookShow';

function BookList({ books, onDelete, onEdit }) {
    /**
     * Display list of books.
     * @param book book added to list
     */
    const renderedBooks = books.map((book) => {
        return <BookShow key={book.id} book={book} onDelete={onDelete} onEdit={onEdit}/>;
    });

    return (
        <div className='book-list'>
            {renderedBooks}
        </div>
    );
}

export default BookList;