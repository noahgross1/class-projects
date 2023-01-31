import axios from 'axios';
import { useState, useEffect } from 'react';
import BookCreate from './components/BookCreate';
import BookList from './components/BookList';

function App() {
    const [books, setBooks] = useState([]);

    /**
     * Get list of books from database
     */
    const fetchBooks = async () => {
        const response = await axios.get('http://localhost:3001/books');
        setBooks(response.data);
    };

    /**
     * Called when App is first rendered on screen
     */
    useEffect(() => {
        fetchBooks();
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

    return (
        <div className='app'>
            <h1>Reading List</h1>
            <BookList books={books} onDelete={deleteBookById} onEdit={editBookById} />
            <BookCreate onCreate={createBook} />
        </div>
    );
}

export default App;