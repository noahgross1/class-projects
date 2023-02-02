import { createContext, useState, useCallback } from "react";
import axios from "axios";

const BooksContext = createContext();

function Provider({ children }) {
    const [books, setBooks] = useState([]);

    /**
     * Get list of books from database
     */
    const fetchBooks = useCallback(async () => {
        const response = await axios.get('http://localhost:3001/books');
        setBooks(response.data);
    }, []);

    /**
     * Delete book with same id 
     * @param id id of book to be deleted from list
     */
    const deleteBookById = async (id) => {
        await axios.delete(`http://localhost:3001/books/${id}`);

        // Creates new array
        const updatedBooks = books.filter((book) => {
            return book.id !== id;
        });

        setBooks(updatedBooks);
    };

    /**
     * Add book to end of list
     * @param title title of book submitted
     */
    const createBook = async (title) => {
        const response = await axios.post('http://localhost:3001/books', {
            title,
        });

        const updatedBooks = [...books, response.data];
        setBooks(updatedBooks);
    };


    /**
     * Make request to update book.
     * @param id of book to edit
     * @param title new title of book
     */
    const editBookById = async (id, newTitle) => {
        const response = await axios.put(`http://localhost:3001/books/${id}`, {
            title: newTitle,
        });

        const updatedBooks = books.map((book) => {
            if (book.id === id) {
                return { ...book, ...response.data};
            }

            return book;
        });

        setBooks(updatedBooks);
    };

    const valueToShare = {
        books,
        deleteBookById,
        editBookById,
        createBook,
        fetchBooks,
    };

    return (
        <BooksContext.Provider value={valueToShare}>
            {children}
        </BooksContext.Provider>
    );
}

// Named export 
export { Provider };
// Default export
export default BooksContext;

// import BooksContext, { Provider } from ''