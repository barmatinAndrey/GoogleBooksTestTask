package com.example.googlebookstesttask.Model;

import java.util.List;

public class BooksApiResponse {
    private String totalItems;
    private List<items> itemsList;

    public List<items> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<items> itemsList) {
        this.itemsList = itemsList;
    }

    public String getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(String totalItems) {
        this.totalItems = totalItems;
    }

    public class items {
        private volumeInfo volumeInfo;

        public items.volumeInfo getVolumeInfo() {
            return volumeInfo;
        }

        public void setVolumeInfo(items.volumeInfo volumeInfo) {
            this.volumeInfo = volumeInfo;
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
