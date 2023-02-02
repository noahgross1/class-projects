import BookShow from './BookShow';
import useBooksContext from '../hooks/use-books-context';

function BookList() {
    const { books } = useBooksContext();

    /**
     * Display list of books.
     * @param book book added to list
     */
    const renderedBooks = books.map((book) => {
        return <BookShow key={book.id} book={book} />;
    });

    return (
        <div className='book-list'>
            {renderedBooks}
        </div>
    );
}

export default BookList;