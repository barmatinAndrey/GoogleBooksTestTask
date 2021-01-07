package com.example.googlebookstesttask.Model;

import java.util.ArrayList;
import java.util.List;

public class BooksApiResponse {
    private String totalItems;
    private List<BookItem> items;

    public BooksApiResponse() {
        items = new ArrayList<>();
    }


    public String getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(String totalItems) {
        this.totalItems = totalItems;
    }

    public List<BookItem> getItems() {
        return items;
    }

    public void setItems(List<BookItem> items) {
        this.items = items;
    }


    public class BookItem {
        private String id;
        private boolean ifInFavourites;
        private volumeInfo volumeInfo;

        public BookItem.volumeInfo getVolumeInfo() {
            return volumeInfo;
        }

        public void setVolumeInfo(BookItem.volumeInfo volumeInfo) {
            this.volumeInfo = volumeInfo;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isIfInFavourites() {
            return ifInFavourites;
        }

        public void setIfInFavourites(boolean ifInFavourites) {
            this.ifInFavourites = ifInFavourites;
        }


        public class volumeInfo {
            private String title;
            private String subtitle;
            private List<String> authors;
            private imageLinks imageLinks;
            private String previewLink;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getSubtitle() {
                return subtitle;
            }

            public void setSubtitle(String subtitle) {
                this.subtitle = subtitle;
            }

            public List<String> getAuthors() {
                return authors;
            }

            public void setAuthors(List<String> authors) {
                this.authors = authors;
            }

            public imageLinks getImageLinks() {
                return imageLinks;
            }

            public void setImageLinks(imageLinks imageLinks) {
                this.imageLinks = imageLinks;
            }

            public String getPreviewLink() {
                return previewLink;
            }

            public void setPreviewLink(String previewLink) {
                this.previewLink = previewLink;
            }

            public class imageLinks {
                private String thumbnail;

                public String getThumbnail() {
                    return thumbnail;
                }

                public void setThumbnail(String thumbnail) {
                    this.thumbnail = thumbnail;
                }
            }

        }

    }
}
