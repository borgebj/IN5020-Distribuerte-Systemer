import matplotlib.pyplot as plt
import os
import numpy as np

# Directory where the files are located
output_dir = os.path.join('..', 'output', 'results')

# List of server files to read
file_names = ['server1.txt', 'server2.txt', 'server3.txt', 'server4.txt', 'server5.txt']
labels = ['Server 1', 'Server 2', 'Server 3', 'Server 4', 'Server 5']

# Prepare a color map for different plots
colors = ['b', 'g', 'r', 'c', 'm']

# Number of bins for averaging
num_bins = 50

# Create a figure for the combined plot
plt.figure(figsize=(10, 6))

# For individual plots
for file_name, color, label in zip(file_names, colors, labels):
    file_path = os.path.join(output_dir, file_name)

    # Check if the file exists
    if not os.path.isfile(file_path):
        print(f"File not found: {file_name}. Skipping.")
        continue

    timestamps = []
    queue_sizes = []

    # Read the file and extract timestamps and queue sizes
    with open(file_path, "r") as file:
        for line in file:
            try:
                parts = line.strip().split(":")
                timestamp = int(parts[0])   # Unix timestamp
                queue_size = int(parts[1])  # Queue size

                timestamps.append(timestamp)
                queue_sizes.append(queue_size)
            except (IndexError, ValueError):
                print(f'Skipping invalid line: {line}')

    # Convert to NumPy arrays for easier manipulation
    timestamps = np.array(timestamps, dtype=np.float64)  # Cast to float64 to avoid overflow
    queue_sizes = np.array(queue_sizes)

    # Bin the data
    bin_indices = np.linspace(0, len(timestamps) - 1, num_bins + 1).astype(int)

    # Calculate average timestamp and average queue size for each bin
    x_bins = [(timestamps[bin_indices[i]] + timestamps[bin_indices[i + 1] - 1]) / 2.0 for i in range(num_bins)]
    y_bins = [np.mean(queue_sizes[bin_indices[i]:bin_indices[i + 1]]) for i in range(num_bins)]

    # Plotting the binned data on the combined plot
    plt.plot(x_bins, y_bins, marker="o", linestyle="-", color=color, label=label)

    # Create an individual plot for each server
    plt.figure(figsize=(10, 6))
    plt.plot(x_bins, y_bins, marker="o", linestyle="-", color=color)
    plt.title(f"{label} - Binned Queue Size over Time")
    plt.xlabel("Unix Timestamp")
    plt.ylabel("Average Queue Size")
    plt.grid(True)
    plt.tight_layout()

    # Save individual plots
    individual_plot_path = os.path.join(output_dir, f"{file_name}_graph.png")
    plt.savefig(individual_plot_path)
    print(f"Saved individual plot: {individual_plot_path}")
    plt.close()  # Close the individual plot

# Customize the combined plot
plt.title("Combined Binned Queue Size over Time for All Servers")
plt.xlabel("Unix Timestamp")
plt.ylabel("Average Queue Size")
plt.grid(True)
plt.legend()
plt.tight_layout()

# Save the combined plot
combined_plot_path = os.path.join(output_dir, "combined_graph.png")
plt.savefig(combined_plot_path)
print(f"Saved combined plot: {combined_plot_path}")

# Show the combined plot
plt.show()
